import {
    faAward,
    faCheckCircle,
    faEdit,
    faExclamationCircle,
    faInfoCircle,
    faMedal,
    faPlus,
    faSignOutAlt,
    faStar,
    faTimes,
    faTrash,
    faTrophy,
    faUser,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/AchievementManagement.css";
import "../assets/css/Dashboard.css";
import logo from "../assets/images/logo.png";

const AchievementManagement = () => {
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [editingItem, setEditingItem] = useState(null);
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notification, setNotification] = useState({ show: false, message: "", type: "" });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [activeSection, setActiveSection] = useState("achievements");
  const [showDropdown, setShowDropdown] = useState(false);
  const [user, setUser] = useState({ firstName: "Admin", lastName: "User", id: 1 });
  
  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);
  const navigate = useNavigate();
  
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
    if (section === "users") {
      navigate("/user-management");
    } else {
      setActiveSection(section);
    }
  };

  const handleLogout = () => {
    // Static version: Just navigate to login page
    navigate("/login");
  };

  // Function to open modal with specific type
  const openModal = (type, item = null) => {
    setModalType(type);
    setEditingItem(item);
    setShowModal(true);
  };

  // Function to handle row click for editing
  const handleRowClick = (item) => {
    openModal("achievements", item);
  };
  
  // Function to delete an item
  const handleDelete = async (achievement) => {
    setItemToDelete(achievement);
    setShowDeleteModal(true);
  };

  // Function to confirm deletion
  const confirmDelete = async () => {
    try {
      // In a real app, this would make an API call to delete the achievement
      // For now, just filter out the achievement from the list
      const updatedAchievements = achievements.filter(achievement => achievement.id !== itemToDelete.id);
      setAchievements(updatedAchievements);
      
      setShowDeleteModal(false);
      setItemToDelete(null);
      
      setNotification({
        show: true,
        message: `Achievement "${itemToDelete.name}" has been successfully deleted.`,
        type: "success"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    } catch (error) {
      console.error("Error deleting achievement:", error);
      setNotification({
        show: true,
        message: "Failed to delete achievement. Please try again.",
        type: "error"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    }
  };

  // Render modal content based on type
  const renderModalContent = () => {
    const isEditing = editingItem !== null;

    return (
      <>
        <h3>{isEditing ? "Edit Achievement" : "Add New Achievement"}</h3>
        <form className="modal-form" onSubmit={handleAddAchievement}>
          <div className="form-group">
            <label htmlFor="name">Achievement Name</label>
            <input
              type="text"
              id="name"
              placeholder="Enter achievement name"
              defaultValue={isEditing ? editingItem.name : ""}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              placeholder="Enter achievement description"
              defaultValue={isEditing ? editingItem.description : ""}
              required
              rows={3}
            />
          </div>
          <div className="form-group">
            <label htmlFor="points">Points</label>
            <input
              type="number"
              id="points"
              placeholder="Enter points"
              defaultValue={isEditing ? editingItem.points : ""}
              min="0"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="icon">Icon</label>
            <select
              id="icon"
              defaultValue={isEditing ? editingItem.icon : ""}
              required
            >
              <option value="">Select an icon</option>
              <option value="faTrophy">Trophy</option>
              <option value="faStar">Star</option>
              <option value="faMedal">Medal</option>
              <option value="faAward">Award</option>
            </select>
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

  // Delete confirmation modal
  const renderDeleteModal = () => {
    if (!showDeleteModal || !itemToDelete) return null;
    
    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={deleteModalRef}>
          <h3>Confirm Deletion</h3>
          <p>Are you sure you want to delete the achievement "{itemToDelete.name}"?</p>
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

  // Dropdown component
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

  const handleAddAchievement = (e) => {
    e.preventDefault();
    
    // Get form values
    const name = e.target.name.value;
    const description = e.target.description.value;
    const points = parseInt(e.target.points.value, 10);
    const icon = e.target.icon.value;
    
    // Create new achievement object
    const newAchievement = {
      id: editingItem ? editingItem.id : Date.now(), // Use existing ID or generate a temporary one
      name,
      description,
      points,
      icon,
      createdDate: editingItem ? editingItem.createdDate : new Date().toISOString()
    };
    
    if (editingItem) {
      // Update existing achievement
      const updatedAchievements = achievements.map(achievement => 
        achievement.id === editingItem.id ? newAchievement : achievement
      );
      setAchievements(updatedAchievements);
      setNotification({
        show: true,
        message: "Achievement updated successfully!",
        type: "success"
      });
    } else {
      // Add new achievement
      setAchievements([...achievements, newAchievement]);
      setNotification({
        show: true,
        message: "Achievement added successfully!",
        type: "success"
      });
    }
    
    // Close modal
    setShowModal(false);
    setEditingItem(null);
    
    // Auto-hide notification
    setTimeout(() => {
      setNotification({ show: false, message: "", type: "" });
    }, 5000);
  };

  // Load static achievement data
  useEffect(() => {
    // Static achievement data for demonstration
    const sampleAchievements = [
      {
        id: 1,
        name: "First Lesson Completed",
        description: "Complete your first pronunciation lesson",
        points: 10,
        icon: "faTrophy",
        createdDate: "2023-09-15T10:30:00Z"
      },
      {
        id: 2,
        name: "Perfect Score",
        description: "Get 100% score in any lesson",
        points: 25,
        icon: "faStar",
        createdDate: "2023-08-20T14:45:00Z"
      },
      {
        id: 3,
        name: "Pronunciation Master",
        description: "Complete 10 different lessons with 90% or higher score",
        points: 50,
        icon: "faMedal",
        createdDate: "2023-09-05T09:15:00Z"
      },
      {
        id: 4,
        name: "Practice Makes Perfect",
        description: "Practice the same lesson 5 times",
        points: 15,
        icon: "faAward",
        createdDate: "2023-07-12T11:20:00Z"
      },
      {
        id: 5,
        name: "Weekly Streak",
        description: "Complete at least one lesson every day for a week",
        points: 30,
        icon: "faStar",
        createdDate: "2023-06-28T16:10:00Z"
      }
    ];
    
    setAchievements(sampleAchievements);
    setLoading(false);
  }, []);

  // Render icon based on string
  const renderIconComponent = (iconName) => {
    switch(iconName) {
      case 'faTrophy':
        return <FontAwesomeIcon icon={faTrophy} />;
      case 'faStar':
        return <FontAwesomeIcon icon={faStar} />;
      case 'faMedal':
        return <FontAwesomeIcon icon={faMedal} />;
      case 'faAward':
        return <FontAwesomeIcon icon={faAward} />;
      default:
        return <FontAwesomeIcon icon={faAward} />;
    }
  };

  const renderAchievementsTable = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan="6">Loading...</td>
        </tr>
      );
    }

    if (achievements.length === 0) {
      return (
        <tr>
          <td colSpan="6">No achievements found.</td>
        </tr>
      );
    }

    return achievements.map((achievement) => (
      <tr key={achievement.id}>
        <td className="achievement-icon">
          <div className="achievement-icon-container">
            {renderIconComponent(achievement.icon)}
          </div>
        </td>
        <td>{achievement.name}</td>
        <td className="achievement-description">{achievement.description}</td>
        <td className="achievement-points">{achievement.points} pts</td>
        <td>{new Date(achievement.createdDate).toLocaleDateString()}</td>
        <td className="action-buttons-cell">
          <button
            className="action-btn blue-btn"
            style={{ backgroundColor: "#4f46e5" }}
            onClick={(e) => {
              e.stopPropagation();
              openModal("achievements", achievement);
            }}
          >
            <FontAwesomeIcon icon={faEdit} /> Edit
          </button>
          <button
            className="action-btn blue-btn"
            style={{ backgroundColor: "rgba(229, 62, 62, 0.8)", color: "white" }}
            onClick={(e) => {
              e.stopPropagation();
              handleDelete(achievement);
            }}
          >
            <FontAwesomeIcon icon={faTrash} /> Delete
          </button>
        </td>
      </tr>
    ));
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
            <h1>ADMIN DASHBOARD</h1>
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
                className={activeSection === "users" ? "active" : ""}
                onClick={() => handleNavClick("users")}
              >
                <FontAwesomeIcon icon={faUsers} className="sidebar-icon" />
                Users Management
              </li>
              <li 
                className={activeSection === "achievements" ? "active" : ""}
                onClick={() => handleNavClick("achievements")}
              >
                <FontAwesomeIcon icon={faAward} className="sidebar-icon" />
                Achievements
              </li>
            </ul>
          </nav>
        </aside>

        <main className="content">
          <h2 className="dashboard-title">Achievements Management</h2>
          <div className="section-header">
            <button className="add-button" onClick={() => openModal('achievements')}>
              <FontAwesomeIcon icon={faPlus} /> Add New Achievement
            </button>
          </div>
          <div className="existing-items">
            <h3>Existing Achievements</h3>
            <table className="items-table">
              <thead>
                <tr>
                  <th>Icon</th>
                  <th>Name</th>
                  <th>Description</th>
                  <th>Points</th>
                  <th>Created Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {renderAchievementsTable()}
              </tbody>
            </table>
          </div>
        </main>
      </div>

      {renderDropdown()}
      {renderModal()}
      {renderDeleteModal()}
      {renderNotification()}
    </div>
  );
};

export default AchievementManagement;