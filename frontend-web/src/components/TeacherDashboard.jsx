import {
  faBookOpen,
  faChartLine,
  faEdit,
  faFolder,
  faPlus,
  faSignOutAlt,
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
  const [user, setUser] = useState({ firstName: "", lastName: "", id: null }); // Include `id` in the user state
  const [showDropdown, setShowDropdown] = useState(false);
  const [activeSection, setActiveSection] = useState("lessons"); // Default to lessons instead of dashboard
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [editingItem, setEditingItem] = useState(null);
  const [categories, setCategories] = useState([]); // State to store categories
  const [loading, setLoading] = useState(true); // State to handle loading
  const [lessons, setLessons] = useState([]); // Ensure lessons is initialized as an empty array
  const [analyticsLessons, setAnalyticsLessons] = useState([]);
  const [analyticsCategory, setAnalyticsCategory] = useState(""); // For filtering
  // Notification state
  const [notification, setNotification] = useState({
    show: false,
    message: '',
    type: 'success'
  });
  
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);
  const modalRef = useRef(null);
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

  const showNotification = (message, type = 'success') => {
    setNotification({
      show: true,
      message,
      type
    });
    // Auto-hide notification after 3 seconds
    setTimeout(() => {
      setNotification(prev => ({ ...prev, show: false }));
    }, 3000);
  };

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
    if (window.confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      try {
        await deleteCategory(category.categoryId); // Call the deleteCategory service
        showNotification("Category deleted successfully!");
  
        // Refresh the categories list
        const updatedCategories = await getAllCategories();
        setCategories(updatedCategories);
      } catch (error) {
        console.error("Error deleting category:", error);
        showNotification("Failed to delete category. Please try again.", "error");
      }
    }
  };

  const handleDeleteLesson = async (lesson) => {
    if (window.confirm(`Are you sure you want to delete the lesson "${lesson.name}"?`)) {
      try {
        await deleteLesson(lesson.lessonId); // Call the deleteLesson service
        showNotification("Lesson deleted successfully!");
  
        // Refresh the lessons list
        const updatedLessons = await getAllLessons();
        setLessons(updatedLessons);
      } catch (error) {
        console.error("Error deleting lesson:", error);
        showNotification("Failed to delete lesson. Please try again.", "error");
      }
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

  // Render notification
  const renderNotification = () => {
    if (!notification.show) return null;

    return (
      <div className="notification-overlay">
        <div className={`notification-modal ${notification.type}`}>
          <div className="notification-icon">
            {notification.type === 'success' && '✓'}
            {notification.type === 'error' && '✗'}
            {notification.type === 'info' && 'ℹ'}
          </div>
          <div className="notification-content">
            <p>{notification.message}</p>
            <button 
              onClick={() => setNotification(prev => ({ ...prev, show: false }))} 
              className="notification-button"
            >
              Close
            </button>
          </div>
        </div>
      </div>
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
        showNotification("Error loading categories. Please try refreshing the page.", "error");
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
          showNotification("Received invalid lesson data format.", "error");
        }
      } catch (error) {
        console.error("Error fetching lessons:", error);
        setLessons([]); // Fallback to an empty array in case of an error
        showNotification("Error loading lessons. Please try refreshing the page.", "error");
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
        onClick={(e) => {
          e.stopPropagation();
          openModal("categories", category); // Open modal for editing
        }}
      >
        <FontAwesomeIcon icon={faEdit} /> Edit
      </button>
      <button
        className="action-btn blue-btn"
        onClick={(e) => {
          e.stopPropagation();
          handleDelete(category); // Call handleDelete when the trash icon is clicked
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
    <td className="action-buttons-cell">
      <button
        className="action-btn blue-btn"
        onClick={(e) => {
          e.stopPropagation();
          openModal("lessons", lesson); // Open modal for editing
        }}
      >
        <FontAwesomeIcon icon={faEdit} /> Edit
      </button>
      {/* Add Words Button - now in the middle */}
      <button
        className="words-btn action-btn blue-btn"
        onClick={() => navigate(`/words/${lesson.lessonId}`, { state: { lessonName: lesson.name } })}
      >
        Manage Words
      </button>
      <button
        className="action-btn blue-btn"
        onClick={(e) => {
          e.stopPropagation();
          handleDeleteLesson(lesson); // Call handleDeleteLesson when the trash icon is clicked
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
      showNotification("Session expired. Please login again.", "error");
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
        showNotification("Category updated successfully!");
      } else {
        // Call the createCategory function if adding a new category
        await createCategory(newCategory, user.id);
        showNotification("Category added successfully!");
      }

      // Refresh the categories list
      const updatedCategories = await getAllCategories();
      setCategories(updatedCategories);

      // Close the modal
      setShowModal(false);
    } catch (error) {
      console.error("Error saving category:", error);
      showNotification("Failed to save category. Please try again.", "error");
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
        showNotification("Lesson updated successfully!");
      } else {
        // Call the createLesson function if adding a new lesson
        await createLesson(newLesson, user.id);
        showNotification("Lesson added successfully!");
      }

      // Refresh the lessons list
      const updatedLessons = await getAllLessons();
      setLessons(updatedLessons);

      // Close the modal
      setShowModal(false);
    } catch (error) {
      console.error("Error saving lesson:", error);
      showNotification("Failed to save lesson. Please try again.", "error");
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
      showNotification("Failed to load analytics data.", "error");
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

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="container">
          <div className="logo">
            <Link to="/">
              <img src={logo} alt="Pronounceit Logo" />
            </Link>
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
                Add Lessons
              </li>
              <li 
                className={activeSection === "categories" ? "active" : ""}
                onClick={() => handleNavClick("categories")}
              >
                <FontAwesomeIcon icon={faFolder} className="sidebar-icon" />
                Add Categories
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
      {renderNotification()}
    </div>
  );
};

export default TeacherDashboard;