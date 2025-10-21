import {
  faAward,
  faCheckCircle,
  faEdit,
  faExclamationCircle,
  faInfoCircle,
  faPlus,
  faSignOutAlt,
  faTimes,
  faTrash,
  faUser,
  faUsers
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/AchievementManagement.css";
import "../assets/css/AchievementResponsive.css";
import adminIcon from "../assets/images/adminicon.png";
import logo from "../assets/images/logo.png";
import {
  createAchievement,
  deleteAchievement,
  getAllAchievements,
  updateAchievement
} from "../services/achievementService";

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
  const [previewImage, setPreviewImage] = useState(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const fileInputRef = useRef(null);
  
  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);
  const navigate = useNavigate();

  // Reset preview image when modal opens/closes
  useEffect(() => {
    if (showModal) {
      if (editingItem && editingItem.badgeUrl) {
        setPreviewImage(editingItem.badgeUrl);
      } else {
        setPreviewImage(null);
      }
    }
  }, [showModal, editingItem]);
  
  // Load achievements from backend on component mount
  useEffect(() => {
    const fetchAchievements = async () => {
      try {
        const data = await getAllAchievements();
        setAchievements(data);
      } catch (error) {
        console.error("Error fetching achievements:", error);
        setNotification({
          show: true,
          message: "Failed to load achievements. Please try again.",
          type: "error"
        });
      } finally {
        setLoading(false);
      }
    };

    fetchAchievements();
  }, []);
  
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
        setPreviewImage(null);
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

  // Close sidebar when clicking outside on mobile
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth > 768 && isSidebarOpen) {
        setIsSidebarOpen(false);
      }
    };

    window.addEventListener("resize", handleResize);
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, [isSidebarOpen]);

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
    localStorage.removeItem("token");
    navigate("/login");
  };

  // Function to handle image selection
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Check file size - limit to 5MB
      const maxSize = 5 * 1024 * 1024; // 5MB in bytes
      if (file.size > maxSize) {
        setNotification({
          show: true,
          message: "Image is too large. Please select an image smaller than 5MB.",
          type: "error"
        });
        
        // Reset file input
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
        return;
      }
      
      // Preview the image
      const reader = new FileReader();
      reader.onload = () => {
        setPreviewImage(reader.result);
      };
      reader.readAsDataURL(file);
    }
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
      await deleteAchievement(itemToDelete.id);
      
      // Update the local state
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
          
          {/* Badge Image Upload */}
          <div className="form-group">
            <label htmlFor="badgeImage">Badge Image</label>
            <div className="badge-image-container">
              {previewImage ? (
                <div className="badge-preview">
                  <img src={previewImage} alt="Badge Preview" />
                  <button 
                    type="button" 
                    className="remove-badge-btn"
                    onClick={() => {
                      setPreviewImage(null);
                      if (fileInputRef.current) {
                        fileInputRef.current.value = '';
                      }
                    }}
                  >
                    <FontAwesomeIcon icon={faTimes} />
                  </button>
                </div>
              ) : (
                <div className="badge-upload-placeholder">
                  <FontAwesomeIcon icon={faAward} size="2x" />
                  <span>Upload a badge image</span>
                </div>
              )}
              <input
                type="file"
                id="badgeImage"
                name="badgeImage"
                accept="image/*"
                ref={fileInputRef}
                onChange={handleImageChange}
                className="file-input"
              />
              <button 
                type="button" 
                className="browse-btn"
                onClick={() => fileInputRef.current.click()}
              >
                Browse...
              </button>
            </div>
          </div>
          
          <div className="form-group">
            <label htmlFor="pointsRequired">Points Required to Unlock</label>
            <input
              type="number"
              id="pointsRequired"
              placeholder="Enter points required (e.g., 100, 250, 500)"
              defaultValue={isEditing ? editingItem.pointsRequired : "100"}
              min="1"
              required
            />
            <small className="field-hint">Students will unlock this achievement when they accumulate this many points</small>
          </div>
          <div className="form-group">
            <label htmlFor="isActive">Status</label>
            <select
              id="isActive"
              defaultValue={isEditing ? (editingItem.isActive ? "true" : "false") : "true"}
            >
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
          </div>
          <div className="modal-actions">
            <button
              type="button"
              className="cancel-btn"
              onClick={() => {
                setShowModal(false);
                setPreviewImage(null);
              }}
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

  // Handle form submission for adding/editing achievements
  const handleAddAchievement = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      // Get form values
      const name = e.target.name.value;
      const description = e.target.description.value;
      const pointsRequired = parseInt(e.target.pointsRequired.value, 10);
      const isActive = e.target.isActive.value === "true";
      const badgeFile = e.target.badgeImage.files[0];
      
      // Create achievement data object
      const achievementData = {
        name,
        description,
        pointsRequired,
        isActive,
        badgeFile
      };
      
      if (editingItem) {
        // Update existing achievement
        const updatedAchievement = await updateAchievement(editingItem.id, achievementData);
        
        // Update state with the returned achievement
        setAchievements(achievements.map(item => 
          item.id === editingItem.id ? updatedAchievement : item
        ));
        
        setNotification({
          show: true,
          message: "Achievement updated successfully!",
          type: "success"
        });
      } else {
        // Create new achievement
        const newAchievement = await createAchievement(achievementData);
        
        // Add the new achievement to state
        setAchievements([...achievements, newAchievement]);
        
        setNotification({
          show: true,
          message: "Achievement added successfully!",
          type: "success"
        });
      }
      
      // Close modal and clean up
      setShowModal(false);
      setEditingItem(null);
      setPreviewImage(null);
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
      
    } catch (error) {
      console.error("Error saving achievement:", error);
      setNotification({
        show: true,
        message: `Failed to ${editingItem ? 'update' : 'create'} achievement: ${error.message}`,
        type: "error"
      });
      
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    } finally {
      setLoading(false);
    }
  };

  const renderAchievementsTable = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan="6" style={{ textAlign: 'center', padding: '20px' }}>Loading...</td>
        </tr>
      );
    }

    if (achievements.length === 0) {
      return (
        <tr>
          <td colSpan="6" style={{ textAlign: 'center', padding: '20px' }}>No achievements found.</td>
        </tr>
      );
    }

    return achievements.map((achievement) => (
      <tr key={achievement.id}>
        <td className="achievement-icon">
          <div className="achievement-icon-container">
            {achievement.badgeUrl ? (
              <img 
                src={achievement.badgeUrl} 
                alt={achievement.name}
                className="badge-image"
                onError={(e) => {
                  console.error('Badge image failed to load:', achievement.badgeUrl);
                  e.target.style.display = 'none';
                  e.target.nextSibling.style.display = 'flex';
                }}
                onLoad={() => {
                  console.log('Badge image loaded successfully:', achievement.badgeUrl);
                }}
              />
            ) : null}
            <FontAwesomeIcon 
              icon={faAward} 
              style={{ display: achievement.badgeUrl ? 'none' : 'flex' }}
            />
          </div>
        </td>
        <td>{achievement.name}</td>
        <td className="achievement-description">{achievement.description}</td>
        <td className="achievement-points">{achievement.pointsRequired} pts</td>
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

  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  const closeSidebar = () => {
    setIsSidebarOpen(false);
  };

  return (
    <div className="dashboard-container">
      {/* Sidebar overlay for mobile */}
      <div 
        className={`sidebar-overlay ${isSidebarOpen ? 'active' : ''}`}
        onClick={closeSidebar}
      />
      
      <header className="dashboard-header">
        <div className="container">
          {/* Add hamburger button here, visible on mobile */}
          <button 
            className={`hamburger-menu ${isSidebarOpen ? 'active' : ''}`} 
            onClick={toggleSidebar}
            aria-label="Toggle sidebar"
          >
            <span></span>
            <span></span>
            <span></span>
          </button>
          <div className="logo">
            <a href="/achievement-management" onClick={(e) => { e.preventDefault(); window.location.reload(); }}>
              <img src={logo} alt="Pronounceit Logo" />
            </a>
          </div>
          <div className="dashboard-title-header">
            <h1>ADMIN DASHBOARD</h1>
          </div>
          <div className="user-card" ref={userCardRef} onClick={toggleDropdown}>
            <img src={adminIcon} alt="Admin" className="admin-avatar-icon" />
            <div className="user-info">
              <p>{`${user.firstName} ${user.lastName}`}</p>
            </div>
          </div>
        </div>
      </header>

      <div className="dashboard single">
        <aside className={`sidebar ${isSidebarOpen ? 'active' : ''}`}>
          <nav>
            <ul>
              <li 
                className={activeSection === "users" ? "active" : ""}
                onClick={() => {
                  handleNavClick("users");
                  closeSidebar();
                }}
              >
                <FontAwesomeIcon icon={faUsers} className="sidebar-icon" />
                Users Management
              </li>
              <li 
                className={activeSection === "achievements" ? "active" : ""}
                onClick={() => {
                  handleNavClick("achievements");
                  closeSidebar();
                }}
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
                  <th>Points Required</th>
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