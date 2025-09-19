import {
  faBookOpen,
  faChartLine,
  faCheckCircle,
  faEdit,
  faExclamationCircle,
  faFolder,
  faInfoCircle,
  faPlus,
  faSignOutAlt,
  faTimes,
  faTrash,
  faUser
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/Dashboard.css";
import logo from "../assets/images/logo.png";
import { createCategory, deleteCategory, getAllCategories, updateCategory } from "../services/categoryService"; // Import the service functions
import { createLesson, deleteLesson, getAllLessons, updateLesson } from "../services/lessonService";
import { getAllScoreRecords } from "../services/scoreService";
import { getUserById } from "../services/userService"; // Import the service to fetch user data

const TeacherDashboard = () => {
  const [user, setUser] = useState({ firstName: "", lastName: "", id: null });
  const [showDropdown, setShowDropdown] = useState(false);
  const [activeSection, setActiveSection] = useState("lessons");
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [editingItem, setEditingItem] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [lessons, setLessons] = useState([]);
  const [analyticsLessons, setAnalyticsLessons] = useState([]);
  const [analyticsCategory, setAnalyticsCategory] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [deleteType, setDeleteType] = useState(""); 
  
  // Add notification state
  const [notification, setNotification] = useState({ show: false, message: "", type: "" });
  
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);
  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const navigate = useNavigate();

  const handleLogout = () => {
    // Static version: Just navigate to login page
    navigate("/login");
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target) &&
        userCardRef.current &&
        !userCardRef.current.contains(event.target)
      ) {
        setShowDropdown(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownRef, userCardRef]);

  // Close modal when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        modalRef.current &&
        !modalRef.current.contains(event.target) &&
        showModal
      ) {
        setShowModal(false);
        setEditingItem(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [modalRef, showModal]);

  // Close delete modal when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        deleteModalRef.current &&
        !deleteModalRef.current.contains(event.target) &&
        showDeleteModal
      ) {
        setShowDeleteModal(false);
        setItemToDelete(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [deleteModalRef, showDeleteModal]);

  const toggleDropdown = (e) => {
    e.stopPropagation();
    setShowDropdown(!showDropdown);
  };

  const handleNavClick = (section) => {
    setActiveSection(section);
  };

  // Function to open modal with specific type
  const openModal = (type, item = null) => {
    setModalType(type);
    setEditingItem(item);
    setShowModal(true);
  };

  // Function to handle row click for editing
  const handleRowClick = (item, type) => {
    openModal(type, item);
  };
  
  // Function to delete an item (just a placeholder for now)
  const handleDelete = async (category) => {
    setItemToDelete(category);
    setDeleteType("category");
    setShowDeleteModal(true);
  };

  // Updated handleDeleteLesson function
  const handleDeleteLesson = async (lesson) => {
    setItemToDelete(lesson);
    setDeleteType("lesson");
    setShowDeleteModal(true);
  };

  // New function to process deletion after confirmation
  const confirmDelete = async () => {
    try {
      if (deleteType === "lesson" && itemToDelete) {
        await deleteLesson(itemToDelete.lessonId);
        const updatedLessons = await getAllLessons();
        setLessons(updatedLessons);
        setNotification({
          show: true,
          message: `Lesson "${itemToDelete.name}" has been successfully deleted.`,
          type: "success"
        });
      } else if (deleteType === "category" && itemToDelete) {
        await deleteCategory(itemToDelete.categoryId);
        const updatedCategories = await getAllCategories();
        setCategories(updatedCategories);
        setNotification({
          show: true,
          message: `Category "${itemToDelete.name}" has been successfully deleted.`,
          type: "success"
        });
      }
      setShowDeleteModal(false);
      setItemToDelete(null);
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    } catch (error) {
      console.error(`Error deleting ${deleteType}:`, error);
      setNotification({
        show: true,
        message: `Failed to delete ${deleteType}. Please try again.`,
        type: "error"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    }
  };

  const renderDropdown = () => {
    if (!showDropdown) return null;

    const rect = userCardRef.current?.getBoundingClientRect();
    if (!rect) return null;

    const dropdownStyle = {
      position: "fixed",
      top: `${rect.bottom + 3}px`,
      right: `${window.innerWidth - rect.right}px`,
      background: "white",
      borderRadius: "8px",
      boxShadow: "0 4px 12px rgba(0, 0, 0, 0.15)",
      zIndex: 9999,
      width: "200px",
      padding: "10px 0",
    };

    return ReactDOM.createPortal(
      <div className="user-dropdown-portal" style={dropdownStyle} ref={dropdownRef}>
        <Link to="/profile" className="dropdown-item">
          <FontAwesomeIcon icon={faUser} className="dropdown-icon" />
          Edit Profile
        </Link>
        <button
          onClick={(e) => {
            e.stopPropagation();
            handleLogout();
          }}
          className="dropdown-item"
        >
          <FontAwesomeIcon icon={faSignOutAlt} className="dropdown-icon" />
          Logout
        </button>
      </div>,
      document.body
    );
  };

  // Render modal content based on type
  const renderModalContent = () => {
    const isEditing = editingItem !== null;

    if (modalType === "categories") {
      return (
        <>
          <h3>{isEditing ? "Edit Category" : "Add New Category"}</h3>
          <form className="modal-form" onSubmit={handleAddCategory}>
            <div className="form-group">
              <label htmlFor="categoryName">Category Name</label>
              <input
                type="text"
                id="categoryName"
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
            <div className="modal-actions">
              <button
                type="button"
                className="cancel-btn"
                onClick={() => setShowModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="submit-btn">
                {isEditing ? "Update" : "Add"}
              </button>
            </div>
          </form>
        </>
      );
    }

    if (modalType === "lessons") {
      return (
        <>
          <h3>{isEditing ? "Edit Lesson" : "Add New Lesson"}</h3>
          <form className="modal-form" onSubmit={handleAddLesson}>
            {!isEditing && ( // Only show the category dropdown when not editing
              <div className="form-group">
                <label htmlFor="lessonCategory">Category</label>
                <select
                  id="lessonCategory"
                  defaultValue={isEditing ? editingItem.category.categoryId : ""}
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map((category) => (
                    <option key={category.categoryId} value={category.categoryId}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
            )}
            <div className="form-group">
              <label htmlFor="lessonTitle">Lesson Name</label>
              <input
                type="text"
                id="lessonTitle"
                placeholder="Enter lesson name"
                defaultValue={isEditing ? editingItem.name : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lessonFocus">Focus</label>
              <input
                type="text"
                id="lessonFocus"
                placeholder="Enter lesson focus"
                defaultValue={isEditing ? editingItem.focus : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lessonSequence">Sequence</label>
              <input
                type="number"
                id="lessonSequence"
                placeholder="Enter sequence number"
                defaultValue={isEditing ? editingItem.sequence : ""}
                required
              />
            </div>
            <div className="modal-actions">
              <button
                type="button"
                className="cancel-btn"
                onClick={() => setShowModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="submit-btn">
                {isEditing ? "Update" : "Add"}
              </button>
            </div>
          </form>
        </>
      );
    }

    return null;
  };

  // Modal component
  const renderModal = () => {
    if (!showModal) return null;

    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={modalRef}>
          {renderModalContent()}
        </div>
      </div>,
      document.body
    );
  };

  // Add this new function to render delete confirmation modal
  const renderDeleteModal = () => {
    if (!showDeleteModal || !itemToDelete) return null;
    
    const itemName = deleteType === "lesson" ? itemToDelete.name : itemToDelete.name;
    const itemType = deleteType === "lesson" ? "lesson" : "category";
    
    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={deleteModalRef}>
          <h3>Confirm Deletion</h3>
          <p>Are you sure you want to delete the {itemType} "{itemName}"?</p>
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
              style={{
                backgroundColor: "rgba(229, 62, 62, 0.1)",
                color: "#e53e3e",
                border: "none",
                borderRadius: "5px",
                padding: "10px 20px", 
                cursor: "pointer",
                fontWeight: "500"
              }}
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

  // Content sections based on activeSection state
  const renderContent = () => {
    switch(activeSection) {
      case 'lessons':
        return (
          <>
            <h2 className="dashboard-title">Lessons Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('lessons')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Lesson
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Lessons</h3>
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
      case 'categories':
        return (
          <>
            <h2 className="dashboard-title">Categories Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('categories')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Category
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Categories</h3>
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
                <tbody>
                  {renderCategoriesTable()}
                </tbody>
              </table>
            </div>
          </>
        );
      case 'analytics':
        return renderAnalyticsTable();
      default:
        return (
          <>
            <h2 className="dashboard-title">Lessons Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('lessons')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Lesson
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Lessons</h3>
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
    }
  };

  useEffect(() => {
    // Fetch categories when the component mounts
    const fetchCategories = async () => {
      try {
        const data = await getAllCategories();
        setCategories(data);
      } catch (error) {
        console.error("Error fetching categories:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  useEffect(() => {
    // Fetch lessons when the component mounts
    const fetchLessons = async () => {
      try {
        const data = await getAllLessons();
        if (Array.isArray(data)) {
          setLessons(data); // Only set lessons if data is an array
        } else {
          console.error("Invalid data format for lessons:", data);
          setLessons([]); // Fallback to an empty array
        }
      } catch (error) {
        console.error("Error fetching lessons:", error);
        setLessons([]); // Fallback to an empty array in case of an error
      } finally {
        setLoading(false);
      }
    };

    fetchLessons();
  }, []);

  const renderCategoriesTable = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan="5">Loading...</td>
        </tr>
      );
    }

    if (categories.length === 0) {
      return (
        <tr>
          <td colSpan="5">No categories found.</td>
        </tr>
      );
    }

    return categories.map((category) => (
      <tr key={category.categoryId}>
        <td>{category.name}</td>
        <td>{category.description}</td>
        <td>{`${category.createdBy.firstName} ${category.createdBy.lastName}`}</td>
        <td>{new Date(category.createdDate).toLocaleDateString()}</td>
        <td className="action-buttons-cell">
          <button
            className="action-btn blue-btn"
            style={{ backgroundColor: "#4f46e5" }}
            onClick={(e) => {
              e.stopPropagation();
              openModal("categories", category);
            }}
          >
            <FontAwesomeIcon icon={faEdit} /> Edit
          </button>
          <button
            className="action-btn blue-btn"
            style={{ backgroundColor: "rgba(229, 62, 62, 0.8)", color: "white" }}
            onClick={(e) => {
              e.stopPropagation();
              handleDelete(category);
            }}
          >
            <FontAwesomeIcon icon={faTrash} /> Delete
          </button>
        </td>
      </tr>
    ));
  };

  const renderLessonsTable = () => {
  if (loading) {
    return (
      <tr>
        <td colSpan="7">Loading...</td>
      </tr>
    );
  }

  if (lessons.length === 0) {
    return (
      <tr>
        <td colSpan="7">No lessons found.</td>
      </tr>
    );
  }

  return lessons.map((lesson) => (
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
          style={{ backgroundColor: "#4f46e5" }}
          onClick={(e) => {
            e.stopPropagation();
            openModal("lessons", lesson);
          }}
        >
          <FontAwesomeIcon icon={faEdit} /> Edit
        </button>
        <button
          className="lesson-action-btn manage-words-btn"
          style={{ backgroundColor: "#3b82f6" }}
          onClick={() => navigate(`/words/${lesson.lessonId}`, { state: { lessonName: lesson.name } })}
        >
          <FontAwesomeIcon icon={faFolder} /> Manage Words
        </button>
        <button
          className="lesson-action-btn delete-action-btn"
          style={{ backgroundColor: "rgba(229, 62, 62, 0.8)", color: "white" }}
          onClick={(e) => {
            e.stopPropagation();
            handleDeleteLesson(lesson);
          }}
        >
          <FontAwesomeIcon icon={faTrash} /> Delete
        </button>
      </td>
    </tr>
));
};

  const fetchUserData = async () => {
    try {
      const token = localStorage.getItem("token");
      const storedUser = JSON.parse(localStorage.getItem("user"));
      if (token && storedUser && storedUser.userId) {
        const userData = await getUserById(storedUser.userId, token);
        setUser({
          firstName: userData.firstName,
          lastName: userData.lastName,
          id: userData.id, // Store the user ID
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

  useEffect(() => {
    fetchUserData(); // Fetch user data when the component mounts
  }, []);

  const handleAddCategory = async (e) => {
    e.preventDefault();

    // Get the input values
    const name = e.target.categoryName.value;
    const description = e.target.categoryDescription.value;

    // Prepare the category object
    const newCategory = {
      name,
      description,
      createdBy: { id: user.id }, // Use the user ID from the state
      createdDate: new Date().toISOString(), // Automatically set to today's date
      active: true, // Always set to true
    };

    try {
      if (editingItem) {
        // Call the updateCategory function if editing
        await updateCategory(editingItem.categoryId, newCategory);
        setNotification({
          show: true,
          message: "Category updated successfully!",
          type: "success"
        });
      } else {
        // Call the createCategory function if adding a new category
        await createCategory(newCategory, user.id);
        setNotification({
          show: true,
          message: "Category added successfully!",
          type: "success"
        });
      }

      // Refresh the categories list
      const updatedCategories = await getAllCategories();
      setCategories(updatedCategories);

      // Close the modal
      setShowModal(false);
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    } catch (error) {
      console.error("Error saving category:", error);
      setNotification({
        show: true,
        message: "Failed to save category. Please try again.",
        type: "error"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    }
  };

  const handleAddLesson = async (e) => {
    e.preventDefault();

    // Determine if we are editing or adding a new lesson
    const isEditing = editingItem !== null;

    // Get the input values
    const categoryId = isEditing
      ? editingItem.category.categoryId // Use the existing category ID when editing
      : e.target.lessonCategory?.value; // Get the value from the dropdown when adding

    const name = e.target.lessonTitle.value;
    const focus = e.target.lessonFocus.value;
    const sequence = parseInt(e.target.lessonSequence.value, 10);

    // Prepare the lesson object
    const newLesson = {
      category: { categoryId: parseInt(categoryId, 10) }, // Use the selected or existing category ID
      name,
      focus,
      sequence,
      createdBy: { id: user.id }, // Use the logged-in user's ID
      createdDate: new Date().toISOString(), // Automatically set to today's date
      active: true, // Always set to true
    };

    try {
      if (isEditing) {
        // Call the updateLesson function if editing
        await updateLesson(editingItem.lessonId, newLesson);
        setNotification({
          show: true,
          message: "Lesson updated successfully!",
          type: "success"
        });
      } else {
        // Call the createLesson function if adding a new lesson
        await createLesson(newLesson, user.id);
        setNotification({
          show: true,
          message: "Lesson added successfully!",
          type: "success"
        });
      }

      // Refresh the lessons list
      const updatedLessons = await getAllLessons();
      setLessons(updatedLessons);

      // Close the modal
      setShowModal(false);
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    } catch (error) {
      console.error("Error saving lesson:", error);
      setNotification({
        show: true,
        message: "Failed to save lesson. Please try again.",
        type: "error"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    }
  };

  const fetchAnalyticsData = async () => {
    try {
      const lessons = await getAllLessons();
      const scoreRecords = await getAllScoreRecords();

      // Count attempts per lesson
      const lessonsWithAttempts = lessons.map((lesson) => {
        const attempts = scoreRecords.filter(
          (score) => score.lesson.lessonId === lesson.lessonId
        ).length;
        return {
          ...lesson,
          attempts,
        };
      });

      setAnalyticsLessons(lessonsWithAttempts);
    } catch (error) {
      console.error("Failed to fetch analytics data:", error);
    }
  };

  localStorage.setItem("userId", user.id); // Replace `user.id` with the actual user ID from the login response

  const userId = localStorage.getItem("userId");
  console.log("Retrieved userId:", userId);

  const renderAnalyticsTable = () => {
    const filteredLessons = analyticsCategory
      ? analyticsLessons.filter(l => l.category.categoryId === parseInt(analyticsCategory))
      : analyticsLessons;

    return (
      <div>
        <h2>Lesson Analytics</h2>
        <div style={{ marginBottom: "1rem" }}>
          <label>Filter by Category: </label>
          <select
            value={analyticsCategory}
            onChange={e => setAnalyticsCategory(e.target.value)}
          >
            <option value="">All</option>
            {categories.map(cat => (
              <option key={cat.categoryId} value={cat.categoryId}>{cat.name}</option>
            ))}
          </select>
        </div>
        <table className="analytics-table">
          <thead>
            <tr>
              <th>Lesson Name</th>
              <th>Category</th>
              <th>Attempts</th>
            </tr>
          </thead>
          <tbody>
            {filteredLessons.length === 0 ? (
              <tr>
                <td colSpan="3">No lessons found.</td>
              </tr>
            ) : (
                            filteredLessons.map((lesson) => (
                <tr key={lesson.lessonId}>
                  <td
                    style={{ color: "blue", cursor: "pointer", textDecoration: "underline" }}
                    onClick={() => navigate(`/analytics/${lesson.lessonId}`, { state: { lessonName: lesson.name } })}
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
    )
  };

  useEffect(() => {
    if (activeSection === "analytics") {
      fetchAnalyticsData();
    }
  }, [activeSection]);

  // Add a new function to render notifications
  const renderNotification = () => {
    if (!notification.show) return null;
    
    return ReactDOM.createPortal(
      <div className={`notification-overlay`}>
        <div className={`notification-modal ${notification.type}`}>
          <div className="notification-icon">
            {notification.type === 'success' && <FontAwesomeIcon icon={faCheckCircle} />}
            {notification.type === 'error' && <FontAwesomeIcon icon={faExclamationCircle} />}
            {notification.type === 'info' && <FontAwesomeIcon icon={faInfoCircle} />}
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

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
  <div className="container">
    <div className="logo">
      <Link to="/">
        <img src={logo} alt="Pronounceit Logo" />
      </Link>
    </div>
    <div className="dashboard-title-header">
      <h1>TEACHER DASHBOARD</h1>
    </div>
    <div className="user-card" ref={userCardRef} onClick={toggleDropdown}>
      <div className="default-avatar">
        <span>{user.firstName.charAt(0)}</span>
      </div>
      <div className="user-info">
        <p>{`${user.firstName} ${user.lastName}`}</p>
      </div>
    </div>
  </div>
</header>

      <div className="dashboard single">
        <aside className="sidebar">
          <nav>
            <ul>
              <li 
                className={activeSection === "lessons" ? "active" : ""}
                onClick={() => handleNavClick("lessons")}
              >
                <FontAwesomeIcon icon={faBookOpen} className="sidebar-icon" />
                Lessons
              </li>
              <li 
                className={activeSection === "categories" ? "active" : ""}
                onClick={() => handleNavClick("categories")}
              >
                <FontAwesomeIcon icon={faFolder} className="sidebar-icon" />
                Categories
              </li>
              <li 
                className={activeSection === "analytics" ? "active" : ""}
                onClick={() => handleNavClick("analytics")}
              >
                <FontAwesomeIcon icon={faChartLine} className="sidebar-icon" />
                Student Analytics
              </li>
            </ul>
          </nav>
        </aside>

        <main className="content">
          {renderContent()}
        </main>
      </div>

      {renderDropdown()}
      {renderModal()}
      {renderDeleteModal()}
      {renderNotification()}
    </div>
  );
};

export default TeacherDashboard;

