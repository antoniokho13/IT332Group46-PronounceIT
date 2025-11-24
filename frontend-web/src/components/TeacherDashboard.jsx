import {
  faCheckCircle,
  faEdit,
  faExclamationCircle,
  faInfoCircle,
  faTimes,
  faTrash,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useNavigate } from "react-router-dom";
import "../assets/css/Dashboard.css";
import "../assets/css/Modal.css";
import ModalSuccess from "../layout/ModalSuccess";

// --- ADD THIS NEW IMPORT ---
import "../assets/css/Analytics.css";
// --- END OF ADD ---

// import "../assets/css/DashboardResponsive.css"; // This import is correctly removed
import Header from "../layout/Header";
import SidebarLayout from "../layout/Sidebar";
import { logout } from "../services/authService";
import {
  createCategory,
  deleteCategory,
  getAllCategories,
  updateCategory,
} from "../services/categoryService";
import {
  createLesson,
  deleteLesson,
  getAllLessons,
  updateLesson,
} from "../services/lessonService";
import { getAllScoreRecords } from "../services/scoreService";
import { getUserById } from "../services/userService";

const TeacherDashboard = () => {
  const [user, setUser] = useState({ firstName: "", lastName: "", id: null });
  const [activeSection, setActiveSection] = useState("lessons");
  const [pageTitle, setPageTitle] = useState("Lesson Management");
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState(""); // 'lesson' | 'category'
  const [editingItem, setEditingItem] = useState(null);
  const [categories, setCategories] = useState([]);
  const [lessons, setLessons] = useState([]);
  const [analyticsLessons, setAnalyticsLessons] = useState([]);
  const [analyticsCategory, setAnalyticsCategory] = useState("");
  const [loading, setLoading] = useState(true);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [deleteType, setDeleteType] = useState(""); // 'lesson' | 'category'
  const [notification, setNotification] = useState({
    show: false,
    message: "",
    type: "",
  });
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // ✅ NEW: filter state for lessons
  const [lessonCategoryFilter, setLessonCategoryFilter] = useState("");

  const ITEMS_PER_PAGE = 5;
  const [pageLessons, setPageLessons] = useState(1);
  const [pageCategories, setPageCategories] = useState(1);
  const [pageAnalytics, setPageAnalytics] = useState(1);

  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const navigate = useNavigate();

  /* ===============================
     AUTH / LOGOUT
  =============================== */
  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  /* ===============================
     SIDEBAR TOGGLE
  =============================== */
  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
    document.body.classList.toggle("sidebar-open", !sidebarOpen);
  };
  const closeSidebar = () => setSidebarOpen(false);

  /* ===============================
     NAVIGATION BETWEEN SECTIONS
  =============================== */
  const handleNavClick = (section) => {
    setActiveSection(section);
    setPageTitle(
      section === "lessons"
        ? "Lessons Management"
        : section === "categories"
        ? "Category Management"
        : "Student Analytics"
    );
    // Reset relevant page on switch
    if (section === "lessons") setPageLessons(1);
    if (section === "categories") setPageCategories(1);
    if (section === "analytics") setPageAnalytics(1);
    closeSidebar();
  };

  /* ===============================
     NEW SIDEBAR BUTTON HANDLER
  =============================== */
  const handleAddButtonClick = () => {
    if (activeSection === "lessons") {
      setShowModal(true);
      setModalType("lesson");
      setEditingItem(null);
    } else if (activeSection === "categories") {
      setShowModal(true);
      setModalType("category");
      setEditingItem(null);
    }
    // No action for 'analytics'
  };

  /* ===============================
     FETCH USER DATA
  =============================== */
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (token && storedUser && storedUser.userId) {
          const userData = await getUserById(storedUser.userId, token);
          setUser({
            firstName: userData.firstName,
            lastName: userData.lastName,
            id: userData.id,
          });
        } else {
          throw new Error("User not found in localStorage");
        }
      } catch (error) {
        console.error("Failed to fetch user data:", error);
        localStorage.removeItem("user");
        localStorage.removeItem("token");
        navigate("/login");
      }
    };
    fetchUserData();
  }, [navigate]);

  /* ===============================
     FETCH CATEGORIES & LESSONS
  =============================== */
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [categoriesData, lessonsData] = await Promise.all([
          getAllCategories(),
          getAllLessons(),
        ]);
        setCategories(categoriesData);
        setLessons(lessonsData);
      } catch (error) {
        console.error("Error fetching data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const refreshCategories = async () => {
    try {
      const data = await getAllCategories();
      setCategories(data);
      setPageCategories(1); // keep UX sane after data change
    } catch (error) {
      console.error("Error refreshing categories:", error);
    }
  };

  const refreshLessons = async () => {
    try {
      const data = await getAllLessons();
      setLessons(data);
      setPageLessons(1);
    } catch (error) {
      console.error("Error refreshing lessons:", error);
    }
  };

  /* ===============================
  ADD / UPDATE CATEGORY
  =============================== */
  const handleAddCategory = async (e) => {
    e.preventDefault();
    const name = e.target.categoryName.value;
    const description = e.target.categoryDescription.value;
    const newCategory = {
      name,
      description,
      createdBy: { id: user.id },
      createdDate: new Date().toISOString(),
      active: true,
    };
    try {
      if (editingItem) {
        await updateCategory(editingItem.categoryId, newCategory);
        setNotification({
          show: true,
          message: "Category updated successfully!",
          type: "success",
        });
      } else {
        await createCategory(newCategory, user.id);
        setNotification({
          show: true,
          message: "Category added successfully!",
          type: "success",
        });
      }
      await refreshCategories();
      setShowModal(false);
    } catch (error) {
      console.error("Error saving category:", error);
      setNotification({
        show: true,
        message: "Failed to save category.",
        type: "error",
      });
    }
  };

  /* ===============================  ADD / UPDATE LESSON
  =============================== */
  const handleAddLesson = async (e) => {
    e.preventDefault();
    const isEditing = editingItem !== null;
    const categoryId = isEditing
      ? editingItem.category.categoryId
      : e.target.lessonCategory?.value;
    const name = e.target.lessonTitle.value;
    const focus = e.target.lessonFocus.value;
    const sequence = parseInt(e.target.lessonSequence.value, 10);
    const newLesson = {
      category: { categoryId: parseInt(categoryId, 10) },
      name,
      focus,
      sequence,
      createdBy: { id: user.id },
      createdDate: new Date().toISOString(),
      active: true,
    };
    try {
      if (isEditing) {
        await updateLesson(editingItem.lessonId, newLesson);
        setNotification({
          show: true,
          message: "Lesson updated successfully!",
          type: "success",
        });
      } else {
        await createLesson(newLesson, user.id);
        setNotification({
          show: true,
          message: "Lesson added successfully!",
          type: "success",
        });
      }
      await refreshLessons();
      setShowModal(false);
    } catch (error) {
      console.error("Error saving lesson:", error);
      setNotification({
        show: true,
        message: "Failed to save lesson.",
        type: "error",
      });
    }
  };

  /* ===============================
     DELETE CATEGORY / LESSON
  =============================== */
  const handleDelete = (item, type) => {
    setItemToDelete(item);
    setDeleteType(type);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      if (deleteType === "lesson" && itemToDelete) {
        await deleteLesson(itemToDelete.lessonId);
        await refreshLessons();
        setNotification({
          show: true,
          message: `Lesson "${itemToDelete.name}" deleted successfully!`,
          type: "success",
        });
      } else if (deleteType === "category" && itemToDelete) {
        await deleteCategory(itemToDelete.categoryId);
        await refreshCategories();
        setNotification({
          show: true,
          message: `Category "${itemToDelete.name}" deleted successfully!`,
          type: "success",
        });
      }
      setShowDeleteModal(false);
      setItemToDelete(null);
    } catch (error) {
      console.error("Error deleting:", error);
      setNotification({
        show: true,
        message: `Failed to delete ${deleteType}.`,
        type: "error",
      });
    }
  };

  /* ===============================LYTICS FETCH
  =============================== */
  const fetchAnalyticsData = async () => {
    try {
      const lessonsData = await getAllLessons();
      const scoreRecords = await getAllScoreRecords();
      const lessonsWithAttempts = lessonsData.map((lesson) => {
        const attempts = scoreRecords.filter(
          (s) => s.lesson.lessonId === lesson.lessonId
        ).length;
        return { ...lesson, attempts };
      });
      setAnalyticsLessons(lessonsWithAttempts);
      setPageAnalytics(1);
    } catch (error) {
      console.error("Failed to fetch analytics data:", error);
    }
  };

  useEffect(() => {
    if (activeSection === "analytics") fetchAnalyticsData();
  }, [activeSection]);

  /* ===============================
     NOTIFICATION PORTAL
  =============================== */
  const renderNotification = () => {
    if (!notification.show) return null;
    return ReactDOM.createPortal(
      <div className="notification-overlay">
        <div className={`notification-modal ${notification.type}`}>
          <div className="notification-icon">
            {notification.type === "success" && (
              <FontAwesomeIcon icon={faCheckCircle} />
            )}
            {notification.type === "error" && (
              <FontAwesomeIcon icon={faExclamationCircle} />
            )}
            {notification.type === "info" && (
              <FontAwesomeIcon icon={faInfoCircle} />
            )}
          </div>
          <div className="notification-content">
            <p>{notification.message}</p>
          </div>
          <button
            className="notification-button"
            onClick={() =>
              setNotification({ show: false, message: "", type: "" })
            }
          >
            <FontAwesomeIcon icon={faTimes} />
          </button>
        </div>
      </div>,
      document.body
    );
  };

  /* ===============================
     DELETE MODAL PORTAL
  =============================== */
  const renderDeleteModal = () => {
    if (!showDeleteModal || !itemToDelete) return null;
    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={deleteModalRef}>
          <h3>Confirm Deletion</h3>
          <p>
            Are you sure you want to delete the {deleteType} "
            {itemToDelete.name}"?
          </p>
          <p>This action cannot be undone.</p>
          <div className="modal-actions">
            <button
              type="button"
              className="cancel-btn"
              onClick={() => {
                setShowDeleteModal(false);
                setItemToDelete(null);
              }}
            >
              Cancel
            </button>
            <button
              type="button"
              className="delete-btn"
              onClick={confirmDelete}
            >
              Delete
            </button>
          </div>
        </div>
      </div>,
      document.body
    );
  };

  /* ===============================
     MODAL PORTAL (ADD / EDIT)
  =============================== */
  const renderModal = () => {
    if (!showModal) return null;
    const isLesson = modalType === "lesson";
    const isEditing = editingItem !== null;

    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={modalRef}>
          <h3>
            {isEditing
              ? isLesson
                ? "Edit Lesson"
                : "Edit Category"
              : isLesson
              ? "Add Lessons"
              : "Add Category"}
          </h3>

          <form
            className="modal-form"
            onSubmit={isLesson ? handleAddLesson : handleAddCategory}
          >
            {isLesson ? (
              <>
                {!isEditing && (
                  <div className="form-group">
                    <label htmlFor="lessonCategory">Category</label>
                    <select id="lessonCategory" required>
                      <option value="">Select a category</option>
                      {categories.map((c) => (
                        <option key={c.categoryId} value={c.categoryId}>
                          {c.name}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
                <div className="form-group">
                  <label htmlFor="lessonTitle">Lesson Name</label>
                  <input
                    id="lessonTitle"
                    type="text"
                    placeholder="Enter lesson name"
                    defaultValue={isEditing ? editingItem.name : ""}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="lessonFocus">Focus</label>
                  <input
                    id="lessonFocus"
                    type="text"
                    placeholder="Enter lesson focus"
                    defaultValue={isEditing ? editingItem.focus : ""}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="lessonSequence">Sequence</label>
                  <input
                    id="lessonSequence"
                    type="number"
                    placeholder="Enter sequence number"
                    defaultValue={isEditing ? editingItem.sequence : ""}
                    required
                  />
                </div>
              </>
            ) : (
              <>
                <div className="form-group">
                  <label htmlFor="categoryName">Category Name</label>
                  <input
                    id="categoryName"
                    type="text"
                    placeholder="e.g., Vowel Sounds, Consonants"
                    defaultValue={isEditing ? editingItem.name : ""}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="categoryDescription">Description</label>
                  <textarea
                    id="categoryDescription"
                    placeholder="Describe this category of pronunciation"
                    defaultValue={isEditing ? editingItem.description : ""}
                    required
                  ></textarea>
                </div>
              </>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="cancel-btn"
                onClick={() => {
                  setShowModal(false);
                  setEditingItem(null);
                }}
              >
                Cancel
              </button>
              <button type="submit" className="submit-btn">
                {isEditing ? "Update" : "Add"}
              </button>
            </div>
          </form>
        </div>
      </div>,
      document.body
    );
  };

  /* ===============================
     PAGINATION HELPERS
  =============================== */
  const paginate = (items, page) => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return items.slice(start, start + ITEMS_PER_PAGE);
  };

  const Pagination = ({ total, page, setPage }) => {
    const totalPages = Math.ceil(total / ITEMS_PER_PAGE);
    if (totalPages === 0) return null;

    // Generate page numbers
    const pageNumbers = Array.from({ length: totalPages }, (_, i) => i + 1);

    return (
      <div className="pagination-container right">
        {pageNumbers.map((num) => (
          <button
            key={num}
            className={`pagination-number ${page === num ? "active" : ""}`}
            onClick={() => setPage(num)}
          >
            {num}
          </button>
        ))}
      </div>
    );
  };

  /* ===============================
     LESSONS, CATEGORIES & ANALYTICS TABLES
  =============================== */
  const renderLessonsTable = (lessonList) =>
    loading ? (
      <tr>
        <td colSpan="7">Loading...</td>
      </tr>
    ) : lessonList.length === 0 ? (
      <tr>
        <td colSpan="7">No lessons found.</td>
      </tr>
    ) : (
      paginate(lessonList, pageLessons).map((lesson) => (
        <tr key={lesson.lessonId}>
          <td>{lesson.name}</td>
          <td>{lesson.focus}</td>
          <td>{lesson.sequence}</td>
          <td>{lesson.category.name}</td>
          <td>{`${lesson.createdBy.firstName} ${lesson.createdBy.lastName}`}</td>
          <td>{new Date(lesson.createdDate).toLocaleDateString()}</td>
          <td className="lesson-actions-cell">
            <button
              className="lesson-action-btn"
              style={{ backgroundColor: "#16a34a", color: "white" }}
              onClick={() =>
                navigate(`/words/${lesson.lessonId}`, {
                  state: { lessonName: lesson.name },
                })
              }
            >
              Words
            </button>
            <button
              className="lesson-action-btn"
              onClick={() => {
                setShowModal(true);
                setModalType("lesson");
                setEditingItem(lesson);
              }}
            >
              <FontAwesomeIcon icon={faEdit} />
            </button>
            <button
              className="lesson-action-btn"
              style={{
                backgroundColor: "rgba(229, 62, 62, 0.8)",
                color: "white",
              }}
              onClick={() => handleDelete(lesson, "lesson")}
            >
              <FontAwesomeIcon icon={faTrash} />
            </button>
          </td>
        </tr>
      ))
    );

  const renderCategoriesTable = () =>
    loading ? (
      <tr>
        <td colSpan="5">Loading...</td>
      </tr>
    ) : categories.length === 0 ? (
      <tr>
        <td colSpan="5">No categories found.</td>
      </tr>
    ) : (
      paginate(categories, pageCategories).map((category) => (
        <tr key={category.categoryId}>
          <td>{category.name}</td>
          <td>{category.description}</td>
          <td>{`${category.createdBy.firstName} ${category.createdBy.lastName}`}</td>
          <td>{new Date(category.createdDate).toLocaleDateString()}</td>
          <td>
            <button
              className="lesson-action-btn"
              onClick={() => {
                setShowModal(true);
                setModalType("category");
                setEditingItem(category);
              }}
            >
              <FontAwesomeIcon icon={faEdit} />
            </button>
            <button
              className="lesson-action-btn"
              style={{
                backgroundColor: "rgba(229, 62, 62, 0.8)",
                color: "white",
              }}
              onClick={() => handleDelete(category, "category")}
            >
              <FontAwesomeIcon icon={faTrash} />
            </button>
          </td>
        </tr>
      ))
    );

  /* === MODIFIED THIS FUNCTION === */
  const renderAnalyticsTable = () => {
    const filtered = analyticsCategory
      ? analyticsLessons.filter(
          (l) => l.category.categoryId === parseInt(analyticsCategory, 10)
        )
      : analyticsLessons;

    return (
      <div className="existing-items">
        <div className="table-wrapper">
          <table className="items-table">
            <thead>
              <tr>
                <th>Lesson Name</th>
                <th>Category</th>
                <th>Attempts</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan="3">No lessons found.</td>
                </tr>
              ) : (
                paginate(filtered, pageAnalytics).map((lesson) => (
                  <tr key={lesson.lessonId}>
                    <td
                      className="analytics-lesson-link"
                      onClick={() =>
                        navigate(`/analytics/${lesson.lessonId}`, {
                          state: { lessonName: lesson.name },
                        })
                      }
                    >
                      {lesson.name}
                    </td>
                    <td>{lesson.category.name}</td>
                    <td>{lesson.attempts}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination for Analytics */}
        <Pagination
          total={filtered.length}
          page={pageAnalytics}
          setPage={setPageAnalytics}
        />
      </div>
    );
  };

  /* ===============================
     SECTION RENDER LOGIC
  =============================== */
  const renderContent = () => {
    switch (activeSection) {
      case "lessons": {
        // ✅ apply filter here
        const filteredLessons = lessonCategoryFilter
          ? lessons.filter(
              (l) =>
                l.category.categoryId ===
                parseInt(lessonCategoryFilter, 10)
            )
          : lessons;

        return (
          <div className="existing-items">
            {/* ✅ filter dropdown right above table */}
           <div className="lesson-filter-container">
  <span className="lesson-filter-label">Filter by Category:</span>
  <select
    className="lesson-filter-select"
    value={lessonCategoryFilter}
    onChange={(e) => {
      setLessonCategoryFilter(e.target.value);
      setPageLessons(1);
    }}
  >
    <option value="">All Categories</option>
    {categories.map((c) => (
      <option key={c.categoryId} value={c.categoryId}>
        {c.name}
      </option>
    ))}
  </select>
</div>

            <div className="table-wrapper">
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Lesson Name</th>
                    <th>Focus</th>
                    <th>Sequence</th>
                    <th>Category</th>
                    <th>Created By</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>{renderLessonsTable(filteredLessons)}</tbody>
              </table>
            </div>
            <Pagination
              total={filteredLessons.length}
              page={pageLessons}
              setPage={setPageLessons}
            />
          </div>
        );
      }
      case "categories":
        return (
          <div className="existing-items">
            <div className="table-wrapper">
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Created By</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>{renderCategoriesTable()}</tbody>
              </table>
            </div>
            <Pagination
              total={categories.length}
              page={pageCategories}
              setPage={setPageCategories}
            />
          </div>
        );
      case "analytics":
        return renderAnalyticsTable();
      default:
        return null;
    }
  };

  /* ===============================
     RENDER LAYOUT
  =============================== */
  return (
    <div className="dashboard-container">
      <Header
        isDashboard={true}
        user={user}
        onLogout={handleLogout}
        toggleSidebar={toggleSidebar}
        sidebarOpen={sidebarOpen}
        pageTitle={pageTitle}
      />
      <SidebarLayout
        activeSection={activeSection}
        handleNavClick={handleNavClick}
        sidebarOpen={sidebarOpen}
        onAddButtonClick={handleAddButtonClick} /* === PROP ADDED === */
      >
        {renderContent()}
      </SidebarLayout>

      {renderModal()}
      {renderDeleteModal()}
      {renderNotification()}

      {notification.show && (
        <ModalSuccess
          show={notification.show}
          message={notification.message}
          type={notification.type}
          onClose={() =>
            setNotification({ show: false, message: "", type: "" })
          }
        />
      )}
    </div>
  );
};

export default TeacherDashboard;