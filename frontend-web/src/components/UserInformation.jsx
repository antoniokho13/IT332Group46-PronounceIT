import {
  faArrowLeft,
  faEdit,
  faEnvelope,
  faGraduationCap,
  faLock,
  faSave,
  faTimes,
  faTrash,
  faUser,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/UserInformation.css";
import { deleteUser, getUserById, updateUser } from "../services/userService";

const UserInformation = () => {
  const [userData, setUserData] = useState({ firstName: "", lastName: "", email: "", role: "" });
  const [formData, setFormData] = useState({ firstName: "", lastName: "", email: "", password: "", confirmPassword: "" });
  const [isEditing, setIsEditing] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [saveError, setSaveError] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (token && storedUser && storedUser.userId) {
          const userData = await getUserById(storedUser.userId, token);
          setUserData(userData);
          setFormData({ ...userData, password: "", confirmPassword: "" });
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

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    if (name === "password" || name === "confirmPassword") {
      setPasswordError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password || formData.confirmPassword) {
      if (formData.password !== formData.confirmPassword) {
        setPasswordError("Passwords do not match");
        return;
      }

      if (formData.password.length < 6) {
        setPasswordError("Password must be at least 6 characters");
        return;
      }
    }

    try {
      const { password, confirmPassword, ...dataToUpdate } = formData;
      const token = localStorage.getItem("token");
      const updatedUser = await updateUser(userData.id, { ...dataToUpdate, password }, token);

      setUserData(updatedUser);
      setFormData({ ...updatedUser, password: "", confirmPassword: "" });
      setIsEditing(false);
      setSaveError("");
    } catch (error) {
      console.error("Failed to update user data:", error);
      setSaveError("Failed to save changes. Please try again.");
    }
  };

  const cancelEdit = () => {
    setFormData({ ...userData, password: "", confirmPassword: "" });
    setPasswordError("");
    setSaveError("");
    setIsEditing(false);
  };

  const getRoleDisplayName = (role) => {
    if (role === "USER") return "Student";
    if (role === "ADMIN") return "Admin";
    return "Unknown";
  };

  const handleDeleteAccount = async () => {
    try {
      const token = localStorage.getItem("token");
      await deleteUser(userData.id, token);
      localStorage.removeItem("user");
      localStorage.removeItem("token");
      navigate("/");
    } catch (error) {
      console.error("Failed to delete user account:", error);
    }
  };

  if (!userData.firstName) {
    return <div>Loading...</div>;
  }

  return (
    <div className="profile-container">
      <header className="profile-header">
        <div className="container">
          <div className="logo">
            <Link to="/">
              <img
                src={require("../assets/images/logo.png")}
                alt="Pronounce-IT Logo"
                style={{ height: "60px" }}
              />
            </Link>
          </div>
        </div>
      </header>

      <div className="profile-content">
        <div className={`profile-card ${isEditing ? "editing" : ""}`}>
          <div className="profile-nav">
            {!isEditing && (
              <Link to="/user-dashboard" className="back-button">
                <FontAwesomeIcon icon={faArrowLeft} /> Back to Dashboard
              </Link>
            )}

            <div className="profile-actions">
              {!isEditing ? (
                <>
                  <button className="edit-button" onClick={() => setIsEditing(true)}>
                    <FontAwesomeIcon icon={faEdit} /> Edit Profile
                  </button>
                  <button className="delete-button" onClick={() => setShowDeleteModal(true)}>
                    <FontAwesomeIcon icon={faTrash} /> Delete Account
                  </button>
                </>
              ) : (
                <>
                  <button className="save-button" onClick={handleSubmit}>
                    <FontAwesomeIcon icon={faSave} /> Save
                  </button>
                  <button className="cancel-button" onClick={cancelEdit}>
                    <FontAwesomeIcon icon={faTimes} /> Cancel
                  </button>
                </>
              )}
            </div>
          </div>

          <div className="profile-header-section">
            <h1>{isEditing ? "Edit Profile" : "User Profile"}</h1>
          </div>

          <div className="profile-avatar-section">
            <div className="profile-avatar">
              <span>{userData.firstName.charAt(0)}</span>
            </div>
          </div>

          <div className="profile-details">
            <div className="detail-item">
              <div className="detail-icon">
                <FontAwesomeIcon icon={faUser} />
              </div>
              <div className="detail-content">
                <h3>First Name</h3>
                {isEditing ? (
                  <input
                    type="text"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    className="edit-in-place"
                  />
                ) : (
                  <p>{userData.firstName}</p>
                )}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-icon">
                <FontAwesomeIcon icon={faUser} />
              </div>
              <div className="detail-content">
                <h3>Last Name</h3>
                {isEditing ? (
                  <input
                    type="text"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    className="edit-in-place"
                  />
                ) : (
                  <p>{userData.lastName}</p>
                )}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-icon">
                <FontAwesomeIcon icon={faEnvelope} />
              </div>
              <div className="detail-content">
                <h3>Email</h3>
                {isEditing ? (
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="edit-in-place"
                  />
                ) : (
                  <p>{userData.email}</p>
                )}
              </div>
            </div>

            <div className="detail-item">
              <div className="detail-icon">
                <FontAwesomeIcon icon={faGraduationCap} />
              </div>
              <div className="detail-content">
                <h3>Account Type</h3>
                <p>{getRoleDisplayName(userData.role)}</p>
              </div>
            </div>

            {isEditing && (
              <>
                <div className="detail-item">
                  <div className="detail-icon">
                    <FontAwesomeIcon icon={faLock} />
                  </div>
                  <div className="detail-content">
                    <h3>New Password</h3>
                    <input
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Enter new password"
                      className="edit-in-place"
                    />
                  </div>
                </div>

                <div className="detail-item">
                  <div className="detail-icon">
                    <FontAwesomeIcon icon={faLock} />
                  </div>
                  <div className="detail-content">
                    <h3>Confirm Password</h3>
                    <input
                      type="password"
                      name="confirmPassword"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      placeholder="Confirm new password"
                      className="edit-in-place"
                    />
                    {passwordError && <div className="error-message">{passwordError}</div>}
                  </div>
                </div>
              </>
            )}
          </div>

          {saveError && <div className="error-message">{saveError}</div>}
        </div>
      </div>

      {showDeleteModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Confirm Account Deletion</h2>
            <p>Are you sure you want to delete your account? This action cannot be undone.</p>
            <div className="modal-actions">
              <button className="confirm-button" onClick={handleDeleteAccount}>
                Yes, Delete
              </button>
              <button className="cancel-button" onClick={() => setShowDeleteModal(false)}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserInformation;