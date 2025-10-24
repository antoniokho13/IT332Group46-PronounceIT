
import {
  faCheckCircle,
  faEdit,
  faExclamationCircle,
  faInfoCircle,
  faPlus,
  faTimes,
  faTrash
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useNavigate } from "react-router-dom";
import "../assets/css/Dashboard.css";
import "../assets/css/DashboardResponsive.css";
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
  const [pageTitle, setPageTitle] = useState("Lessons Management");
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
  const [notification, setNotification] = useState({ show: false, message: "", type: "" });
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const navigate = useNavigate();

  // Auth/logout
  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  // Sidebar
  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
    document.body.classList.toggle("sidebar-open", !sidebarOpen);
  };
  const closeSidebar = () => setSidebarOpen(false);

  // Nav
  const handleNavClick = (section) => {
    setActiveSection(section);
    setPageTitle(
      section === "lessons"
        ? "Lessons Management"
        : section === "categories"
        ? "Categories Management"
        : "Student Analytics"
    );
    closeSidebar();
  };

  // Fetch user
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

  // Fetch categories and lessons
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

  // Refresh helpers
  const refreshCategories = async () => {
    try {
      const data = await getAllCategories();
      setCategories(data);
    } catch (error) {
      console.error("Error refreshing categories:", error);
    }
  };
  const refreshLessons = async () => {
    try {
      const data = await getAllLessons();
      setLessons(data);
    } catch (error) {
      console.error("Error refreshing lessons:", error);
    }
  };

  // CRUD: Category
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
        setNotification({ show: true, message: "Category updated successfully!", type: "success" });
      } else {
        await createCategory(newCategory, user.id);
        setNotification({ show: true, message: "Category added successfully!", type: "success" });
      }
      await refreshCategories();
      setShowModal(false);
    } catch (error) {
      console.error("Error saving category:", error);
      setNotification({ show: true, message: "Failed to save category.", type: "error" });
    }
  };

  // CRUD: Lesson
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
        setNotification({ show: true, message: "Lesson updated successfully!", type: "success" });
      } else {
        await createLesson(newLesson, user.id);
        setNotification({ show: true, message: "Lesson added successfully!", type: "success" });
      }
      await refreshLessons();
      setShowModal(false);
    } catch (error) {
      console.error("Error saving lesson:", error);
      setNotification({ show: true, message: "Failed to save lesson.", type: "error" });
    }
  };

  // Delete
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
        setNotification({ show: true, message: `Lesson "${itemToDelete.name}" deleted successfully!`, type: "success" });
      } else if (deleteType === "category" && itemToDelete) {
        await deleteCategory(itemToDelete.categoryId);
        await refreshCategories();
        setNotification({ show: true, message: `Category "${itemToDelete.name}" deleted successfully!`, type: "success" });
      }
      setShowDeleteModal(false);
      setItemToDelete(null);
    } catch (error) {
      console.error("Error deleting:", error);
      setNotification({ show: true, message: `Failed to delete ${deleteType}.`, type: "error" });
    }
  };

  // Analytics
  const fetchAnalyticsData = async () => {
    try {
      const lessonsData = await getAllLessons();
      const scoreRecords = await getAllScoreRecords();
      const lessonsWithAttempts = lessonsData.map((lesson) => {
        const attempts = scoreRecords.filter((s) => s.lesson.lessonId === lesson.lessonId).length;
        return { ...lesson, attempts };
      });
      setAnalyticsLessons(lessonsWithAttempts);
    } catch (error) {
      console.error("Failed to fetch analytics data:", error);
    }
  };
  useEffect(() => {
    if (activeSection === "analytics") fetchAnalyticsData();
  }, [activeSection]);

  // Notifications
  const renderNotification = () => {
    if (!notification.show) return null;
    return ReactDOM.createPortal(
      <div className={`notification-overlay`}>
        <div className={`notification-modal ${notification.type}`}>
          <div className="notification-icon">
            {notification.type === "success" && <FontAwesomeIcon icon={faCheckCircle} />}
            {notification.type === "error" && <FontAwesomeIcon icon={faExclamationCircle} />}
            {notification.type === "info" && <FontAwesomeIcon icon={faInfoCircle} />}
          </div>
          <div className="notification-content">
            <p>{notification.message}</p>
          </div>
          <button
            className="notification-button"
            onClick={() => setNotification({ show: false, message: "", type: "" })}
          >
            <FontAwesomeIcon icon={faTimes} />
          </button>
        </div>
      </div>,
      document.body
    );
  };

  // Modals
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
              ? "Add New Lesson"
              : "Add New Category"}
          </h3>

          <form className="modal-form" onSubmit={isLesson ? handleAddLesson : handleAddCategory}>
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

  const renderDeleteModal = () => {
    if (!showDeleteModal || !itemToDelete) return null;
    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={deleteModalRef}>
          <h3>Confirm Deletion</h3>
          <p>
            Are you sure you want to delete the {deleteType} "{itemToDelete.name}"?
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

  // Tables
  const renderLessonsTable = () =>
    loading ? (
      <tr>
        <td colSpan="7">Loading...</td>
      </tr>
    ) : lessons.length === 0 ? (
      <tr>
        <td colSpan="7">No lessons found.</td>
      </tr>
    ) : (
      lessons.map((lesson) => (
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
              onClick={() => navigate(`/words/${lesson.lessonId}`, { state: { lessonName: lesson.name } })}
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
              style={{ backgroundColor: "rgba(229, 62, 62, 0.8)", color: "white" }}
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
      categories.map((category) => (
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
              style={{ backgroundColor: "rgba(229, 62, 62, 0.8)", color: "white" }}
              onClick={() => handleDelete(category, "category")}
            >
              <FontAwesomeIcon icon={faTrash} />
            </button>
          </td>
        </tr>
      ))
    );

  // Analytics table
  const renderAnalyticsTable = () => {
    const filtered = analyticsCategory
      ? analyticsLessons.filter(
          (l) => l.category.categoryId === parseInt(analyticsCategory, 10)
        )
      : analyticsLessons;

    return (
      <div className="existing-items">
        <div className="existing-header">
          <h3>Lesson Analytics</h3>
          <div>
            <label style={{ marginRight: 8 }}>Filter by Category:</label>
            <select
              value={analyticsCategory}
              onChange={(e) => setAnalyticsCategory(e.target.value)}
            >
              <option value="">All</option>
              {categories.map((cat) => (
                <option key={cat.categoryId} value={cat.categoryId}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>
        </div>

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
              filtered.map((lesson) => (
                <tr key={lesson.lessonId}>
                  <td
                    style={{ color: "blue", cursor: "pointer", textDecoration: "underline" }}
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
    );
  };

  // Content switch
  const renderContent = () => {
    switch (activeSection) {
      case "lessons":
        return (
          <>
            <div className="existing-items">
              <div className="existing-header">
                <h3>Existing Lessons</h3>
                <button
                  className="add-button"
                  onClick={() => {
                    setShowModal(true);
                    setModalType("lesson");
                    setEditingItem(null);
                  }}
                >
                  <FontAwesomeIcon icon={faPlus} /> Add New Lesson
                </button>
              </div>
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
                <tbody>{renderLessonsTable()}</tbody>
              </table>
            </div>
          </>
        );
      case "categories":
        return (
          <>
            <div className="existing-items">
              <div className="existing-header">
                <h3>Existing Categories</h3>
                <button
                  className="add-button"
                  onClick={() => {
                    setShowModal(true);
                    setModalType("category");
                    setEditingItem(null);
                  }}
                >
                  <FontAwesomeIcon icon={faPlus} /> Add New Category
                </button>
              </div>
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
          </>
        );
      case "analytics":
        return renderAnalyticsTable();
      default:
        return null;
    }
  };

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
      >
        {renderContent()}
      </SidebarLayout>
      {renderModal()}
      {renderDeleteModal()}
      {renderNotification()}
    </div>
  );
};

export default TeacherDashboard;
