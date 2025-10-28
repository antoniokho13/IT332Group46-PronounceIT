import {
  faEdit,
  faTrash
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import axios from "axios";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useNavigate } from "react-router-dom";

import Header from "../layout/Header";
import Sidebar from "../layout/Sidebar";

import "../assets/css/Dashboard.css";
import "../assets/css/ModalSuccess.css";
import "../assets/css/UserManagement.css";
import ModalSuccess from "../layout/ModalSuccess";

import { logout } from "../services/authService";
import { deleteUser, updateUser } from "../services/userService";

const API_BASE_URL = "https://it332group46-pronounceit-production.up.railway.app/api/users";

const UserManagement = () => {
  const [showModal, setShowModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // ✅ New states for password validation
  const [confirmPassword, setConfirmPassword] = useState("");
  const [formError, setFormError] = useState("");

  // ✅ ModalSuccess
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successModalData, setSuccessModalData] = useState({
    message: "",
    type: "",
    actionType: "",
  });

  // Pagination
  const [page, setPage] = useState(1);
  const itemsPerPage = 5;

  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const navigate = useNavigate();

  const [user, setUser] = useState({
    firstName: "Admin",
    lastName: "User",
    id: 1,
  });

  // Fetch all users
  const fetchAllUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const token = localStorage.getItem("token");
      if (!token) throw new Error("No authentication token found");

      const response = await axios.get(API_BASE_URL, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setUsers(response.data || []);
    } catch (err) {
      console.error("Error fetching users:", err);
      setError("Failed to load users. Please try again.");
      setSuccessModalData({
        message: "Failed to load users from the server.",
        type: "error",
      });
      setShowSuccessModal(true);
    } finally {
      setLoading(false);
    }
  };

  const refreshUsers = async () => {
    await fetchAllUsers();
  };

  const createUser = async (userData) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) throw new Error("No authentication token found");

      const response = await axios.post(API_BASE_URL, userData, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });
      return response.data;
    } catch (err) {
      console.error("Error creating user:", err);
      throw err;
    }
  };

  const confirmDelete = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) throw new Error("No authentication token found");

      await deleteUser(itemToDelete.id, token);
      await refreshUsers();

      setShowDeleteModal(false);
      setItemToDelete(null);
      setSuccessModalData({
        message: `User "${itemToDelete.firstName} ${itemToDelete.lastName}" deleted successfully.`,
        type: "success",
        actionType: "delete",
      });
      setShowSuccessModal(true);
    } catch (err) {
      console.error("Error deleting user:", err);
      setSuccessModalData({
        message: "Failed to delete user. Please try again.",
        type: "error",
      });
      setShowSuccessModal(true);
    }
  };

  // ✅ Registration-style validation for adding new users
  const handleAddUser = async (e) => {
    e.preventDefault();
    setFormError("");

    try {
      const firstName = e.target.firstName.value;
      const lastName = e.target.lastName.value;
      const email = e.target.email.value;
      const role = e.target.role.value;
      const password = !editingItem ? e.target.password.value : null;
      const confirmPass = !editingItem ? e.target.confirmPassword.value : null;

      if (!editingItem) {
        if (!password || password.length < 6) {
          setFormError("Password must be at least 6 characters.");
          return;
        }
        if (password !== confirmPass) {
          setFormError("Passwords do not match.");
          return;
        }
      }

      const userData = { firstName, lastName, email, role };
      if (!editingItem) userData.password = password;

      const token = localStorage.getItem("token");
      if (!token) throw new Error("No authentication token found");

      if (editingItem) {
        await updateUser(editingItem.id, userData, token);
        setSuccessModalData({
          message: "User updated successfully!",
          type: "success",
          actionType: "edit",
        });
      } else {
        await createUser(userData);
        setSuccessModalData({
          message: "User added successfully!",
          type: "success",
          actionType: "add",
        });
      }

      await refreshUsers();
      setShowModal(false);
      setShowSuccessModal(true);
    } catch (err) {
      console.error("Error saving user:", err);
      setSuccessModalData({
        message: `Failed to ${editingItem ? "update" : "add"} user.`,
        type: "error",
      });
      setShowSuccessModal(true);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const handleNavClick = (section) => {
    if (section === "achievements") navigate("/achievement-management");
  };

  useEffect(() => {
    const currentUserName = localStorage.getItem("firstName") || "Admin";
    const currentUserLastName = localStorage.getItem("lastName") || "User";
    const currentUserId = localStorage.getItem("userId") || "1";
    setUser({ firstName: currentUserName, lastName: currentUserLastName, id: currentUserId });
  }, []);

  useEffect(() => {
    fetchAllUsers();
  }, []);

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(users.length / itemsPerPage));
    if (page > totalPages) setPage(totalPages);
  }, [users.length, page]);

  const totalPages = Math.max(1, Math.ceil(users.length / itemsPerPage));
  const startIndex = (page - 1) * itemsPerPage;
  const currentUsers = users.slice(startIndex, startIndex + itemsPerPage);
  const pageNumbers = Array.from({ length: totalPages }, (_, i) => i + 1);

  // Delete Modal
  const renderDeleteModal = () => {
    if (!showDeleteModal || !itemToDelete) return null;
    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={deleteModalRef}>
          <h3>Confirm Deletion</h3>
          <p>
            Are you sure you want to delete{" "}
            <strong>
              {itemToDelete.firstName} {itemToDelete.lastName}
            </strong>
            ?
          </p>
          <div className="modal-actions">
            <button
              type="button"
              className="cancel-btn"
              onClick={() => setShowDeleteModal(false)}
            >
              Cancel
            </button>
            <button type="button" className="delete-btn" onClick={confirmDelete}>
              Delete
            </button>
          </div>
        </div>
      </div>,
      document.body
    );
  };

  // ✅ Updated Add/Edit Modal (with Confirm Password)
  const renderModal = () => {
    if (!showModal) return null;
    const isEditing = editingItem !== null;

    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={modalRef}>
          <h3>{isEditing ? "Edit User" : "Add New User"}</h3>
          <form className="modal-form" onSubmit={handleAddUser}>
            <div className="form-group">
              <label htmlFor="firstName">First Name</label>
              <input
                type="text"
                id="firstName"
                defaultValue={isEditing ? editingItem.firstName : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lastName">Last Name</label>
              <input
                type="text"
                id="lastName"
                defaultValue={isEditing ? editingItem.lastName : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
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

            {/* ✅ Only for new users */}
            {!isEditing && (
              <>
                <div className="form-group">
                  <label htmlFor="password">Password</label>
                  <input type="password" id="password" minLength="6" required />
                </div>
                <div className="form-group">
                  <label htmlFor="confirmPassword">Confirm Password</label>
                  <input
                    type="password"
                    id="confirmPassword"
                    required
                    onChange={(e) => setConfirmPassword(e.target.value)}
                  />
                </div>
                {formError && (
                  <p
                    style={{
                      color: "#b91c1c",
                      fontSize: "14px",
                      marginTop: "5px",
                    }}
                  >
                    {formError}
                  </p>
                )}
              </>
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
        </div>
      </div>,
      document.body
    );
  };

  const renderUsersTable = () => {
    if (loading)
      return (
        <tr>
          <td colSpan="5" style={{ textAlign: "center" }}>
            Loading users...
          </td>
        </tr>
      );

    if (error)
      return (
        <tr>
          <td colSpan="5" style={{ textAlign: "center", color: "#b91c1c" }}>
            {error}
          </td>
        </tr>
      );

    if (users.length === 0)
      return (
        <tr>
          <td colSpan="5" style={{ textAlign: "center" }}>
            No users found.
          </td>
        </tr>
      );

    return currentUsers.map((u) => (
      <tr key={u.id}>
        <td>{u.firstName}</td>
        <td>{u.lastName}</td>
        <td>{u.email}</td>
        <td>
          <span className={`user-role role-${u.role?.toLowerCase() || "user"}`}>
            {u.role || "USER"}
          </span>
        </td>
        <td className="action-buttons-cell">
          <button
            className="action-btn"
            title="Edit"
            onClick={() => {
              setEditingItem(u);
              setShowModal(true);
            }}
          >
            <FontAwesomeIcon icon={faEdit} />
          </button>
          <button
            className="action-btn delete"
            title="Delete"
            onClick={() => {
              setItemToDelete(u);
              setShowDeleteModal(true);
            }}
          >
            <FontAwesomeIcon icon={faTrash} />
          </button>
        </td>
      </tr>
    ));
  };

  return (
    <>
      <Header
        isDashboard={true}
        user={user}
        onLogout={handleLogout}
        pageTitle="User Management"
        toggleSidebar={() => setSidebarOpen(!sidebarOpen)}
        sidebarOpen={sidebarOpen}
      />

      <Sidebar
        activeSection="users"
        handleNavClick={handleNavClick}
        sidebarOpen={sidebarOpen}
        onAddButtonClick={() => {
          setEditingItem(null);
          setShowModal(true);
        }}
        customItems={[
          { key: "achievements", label: "Achievements" },
          { key: "users", label: "Users" },
        ]}
      >
        <div className="existing-items">
          <div className="table-container">
            <table className="items-table">
              <thead>
                <tr>
                  <th>First name</th>
                  <th>Last name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>{renderUsersTable()}</tbody>
            </table>

            {totalPages > 1 && (
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
            )}
          </div>
        </div>
      </Sidebar>

      {renderModal()}
      {renderDeleteModal()}

      {/* ✅ ModalSuccess */}
      <ModalSuccess
        show={showSuccessModal}
        message={successModalData.message}
        type={successModalData.type}
        role="success"
        actionType={successModalData.actionType}
        onClose={() => setShowSuccessModal(false)}
      />
    </>
  );
};

export default UserManagement;
