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
import axios from "axios";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/Dashboard.css";
import "../assets/css/UserManagement.css";
import logo from "../assets/images/logo.png";
import studentIcon from "../assets/images/studenticon.png";
import { deleteUser, updateUser } from "../services/userService";
import {logout} from "../services/authService";

const API_BASE_URL = "https://it332group46-pronounceit-production.up.railway.app/api/users"; // Base URL for user API

const UserManagement = () => {
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [editingItem, setEditingItem] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notification, setNotification] = useState({ show: false, message: "", type: "" });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [activeSection, setActiveSection] = useState("users");
  const [showDropdown, setShowDropdown] = useState(false);
  const [user, setUser] = useState({ firstName: "Admin", lastName: "User", id: 1 });
  const [error, setError] = useState(null);
  
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
    if (section === "achievements") {
      navigate("/achievement-management");
    } else {
      setActiveSection(section);
    }
  };

  const handleLogout = async () => {
      // This calls your authService.js function.
      // That function will clear localStorage and call the backend.
      await logout(); 
      
      // After the storage is cleared, navigate to the login page.
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
    openModal("users", item);
  };
  
  // Function to delete an item
  const handleDelete = async (user) => {
    setItemToDelete(user);
    setShowDeleteModal(true);
  };

  // Function to confirm deletion
  const confirmDelete = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token found");
      }
      
      // Call the deleteUser function from userService
      await deleteUser(itemToDelete.id, token);
      
      // Refresh users from server
      await refreshUsers();
      
      setShowDeleteModal(false);
      setItemToDelete(null);
      
      setNotification({
        show: true,
        message: `User "${itemToDelete.firstName} ${itemToDelete.lastName}" has been successfully deleted.`,
        type: "success"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    } catch (error) {
      console.error("Error deleting user:", error);
      setNotification({
        show: true,
        message: "Failed to delete user. Please try again.",
        type: "error"
      });
      
      // Auto-hide notification after 5 seconds
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 5000);
    }
  };

  // Add function to get all users
  const fetchAllUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token found");
      }
      
      const response = await axios.get(API_BASE_URL, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      
      setUsers(response.data);
      setLoading(false);
    } catch (error) {
      console.error("Error fetching users:", error);
      setError("Failed to load users. Please try again.");
      setNotification({
        show: true,
        message: "Failed to load users from the server.",
        type: "error"
      });
      setLoading(false);
    }
  };

  // Force refresh function for when users are modified
  const refreshUsers = async () => {
    await fetchAllUsers();
  };

  // Add function to create a new user
  const createUser = async (userData) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token found");
      }
      
      const response = await axios.post(API_BASE_URL, userData, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });
      
      return response.data;
    } catch (error) {
      console.error("Error creating user:", error);
      throw error;
    }
  };

  // Render modal content based on type
  const renderModalContent = () => {
    const isEditing = editingItem !== null;

    return (
      <>
        <h3>{isEditing ? "Edit User" : "Add New User"}</h3>
        <form className="modal-form" onSubmit={handleAddUser}>
          <div className="form-group">
            <label htmlFor="firstName">First Name</label>
            <input
              type="text"
              id="firstName"
              placeholder="Enter first name"
              defaultValue={isEditing ? editingItem.firstName : ""}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="lastName">Last Name</label>
            <input
              type="text"
              id="lastName"
              placeholder="Enter last name"
              defaultValue={isEditing ? editingItem.lastName : ""}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              placeholder="Enter email address"
              defaultValue={isEditing ? editingItem.email : ""}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="role">Role</label>
            <select
              id="role"
              defaultValue={isEditing ? editingItem.role : ""}
              required
            >
              <option value="">Select a role</option>
              <option value="STUDENT">Student</option>
              <option value="TEACHER">Teacher</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          {!isEditing && (
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                type="password"
                id="password"
                placeholder="Enter password"
                required
              />
            </div>
          )}
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
          <p>Are you sure you want to delete the user "{itemToDelete.firstName} {itemToDelete.lastName}"?</p>
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

  // Content sections based on activeSection state
  const renderContent = () => {
    switch(activeSection) {
      case 'users':
        return (
          <>
            <h2 className="dashboard-title">Users Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('users')}>
                <FontAwesomeIcon icon={faPlus} /> Add New User
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Users</h3>
              {error ? (
                <div className="error-message">{error}</div>
              ) : (
                <table className="items-table">
                  <thead>
                    <tr>
                      <th>First Name</th>
                      <th>Last Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Created Date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {renderUsersTable()}
                  </tbody>
                </table>
              )}
            </div>
          </>
        );
      case 'achievements':
        return (
          <>
            <h2 className="dashboard-title">Achievements Management</h2>
            <div className="section-content">
              <p>Achievement management functionality will be implemented soon.</p>
            </div>
          </>
        );
      default:
        return (
          <>
            <h2 className="dashboard-title">Users Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('users')}>
                <FontAwesomeIcon icon={faPlus} /> Add New User
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Users</h3>
              {error ? (
                <div className="error-message">{error}</div>
              ) : (
                <table className="items-table">
                  <thead>
                    <tr>
                      <th>First Name</th>
                      <th>Last Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Created Date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {renderUsersTable()}
                  </tbody>
                </table>
              )}
            </div>
          </>
        );
    }
  };

  const handleAddUser = async (e) => {
    e.preventDefault();
    
    try {
      // Get form values
      const firstName = e.target.firstName.value;
      const lastName = e.target.lastName.value;
      const email = e.target.email.value;
      const role = e.target.role.value;
      
      // Create new user object
      const userData = {
        firstName,
        lastName,
        email,
        role
      };
      
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("No authentication token found");
      }
      
      if (editingItem) {
        // Update existing user via API
        await updateUser(editingItem.id, userData, token);
        
        // Refresh the user list to get updated data
        await refreshUsers();
        
        setNotification({
          show: true,
          message: "User updated successfully!",
          type: "success"
        });
      } else {
        // Add password for new user
        userData.password = e.target.password.value;
        
        // Create new user via API
        await createUser(userData);
        
        // Refresh the user list to get updated data
        await refreshUsers();
        
        setNotification({
          show: true,
          message: "User added successfully!",
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
    } catch (error) {
      console.error("Error saving user:", error);
      setNotification({
        show: true,
        message: `Failed to ${editingItem ? 'update' : 'add'} user: ${error.message || 'Unknown error'}`,
        type: "error"
      });
      
      // Auto-hide notification after longer period for errors
      setTimeout(() => {
        setNotification({ show: false, message: "", type: "" });
      }, 7000);
    }
  };

  // Load users from API when component mounts
  useEffect(() => {
    // Get current user info
    const currentUserName = localStorage.getItem('firstName') || 'Admin';
    const currentUserLastName = localStorage.getItem('lastName') || 'User';
    const currentUserId = localStorage.getItem('userId') || '1';
    
    setUser({
      firstName: currentUserName,
      lastName: currentUserLastName,
      id: currentUserId
    });
    
    // Only fetch users if they haven't been loaded yet
    if (users.length === 0) {
      fetchAllUsers();
    } else {
      setLoading(false);
    }
  }, [users.length]);

  const renderUsersTable = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan="6">Loading users...</td>
        </tr>
      );
    }

    if (users.length === 0) {
      return (
        <tr>
          <td colSpan="6">No users found.</td>
        </tr>
      );
    }

    return users.map((user) => (
      <tr key={user.id}>
        <td>{user.firstName}</td>
        <td>{user.lastName}</td>
        <td>{user.email}</td>
        <td>
          <span className={`user-role role-${(user.role || 'user').toLowerCase()}`}>
            {user.role || 'USER'}
          </span>
        </td>
        <td>{new Date(user.createdDate || user.createdAt || Date.now()).toLocaleDateString()}</td>
        <td className="action-buttons-cell">
          <button
            className="action-btn blue-btn"
            style={{ backgroundColor: "#4f46e5" }}
            onClick={(e) => {
              e.stopPropagation();
              openModal("users", user);
            }}
          >
            <FontAwesomeIcon icon={faEdit} /> Edit
          </button>
          <button
            className="action-btn blue-btn"
            style={{ backgroundColor: "rgba(229, 62, 62, 0.8)", color: "white" }}
            onClick={(e) => {
              e.stopPropagation();
              handleDelete(user);
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
            <a href="/user-management" onClick={(e) => { e.preventDefault(); window.location.reload(); }}>
              <img src={logo} alt="Pronounceit Logo" />
            </a>
          </div>
          <div className="dashboard-title-header">
            <h1>ADMIN DASHBOARD</h1>
          </div>
          <div className="user-card" ref={userCardRef} onClick={toggleDropdown}>
            <img src={studentIcon} alt="User Icon" className="student-avatar-icon" />
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

export default UserManagement;