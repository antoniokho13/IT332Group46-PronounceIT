import {
    faEdit,
    faEnvelope,
    faGraduationCap,
    faLock,
    faSave,
    faTimes,
    faUser
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useState } from "react";
import { Link } from "react-router-dom";
import "../assets/css/UserInformation.css";

const UserInformation = () => {
  // Mock user data that would typically come from registration or API
  const [userData, setUserData] = useState({
    username: "Melgod",
    email: "melgod@example.com",
    role: "student"
  });

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    ...userData,
    password: "",
    confirmPassword: ""
  });
  
  const [passwordError, setPasswordError] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear password error when typing
    if (name === "password" || name === "confirmPassword") {
      setPasswordError("");
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Validate passwords match if both are provided
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
    
    // Update user data but exclude password fields
    const { password, confirmPassword, ...dataToUpdate } = formData;
    setUserData(dataToUpdate);
    setIsEditing(false);
  };

  const cancelEdit = () => {
    setFormData({
      ...userData,
      password: "",
      confirmPassword: ""
    });
    setPasswordError("");
    setIsEditing(false);
  };

  return (
    <div className="profile-container">
      {/* Header with logo */}
      <header className="profile-header">
        <div className="container">
          <div className="logo">
            <Link to="/">
              <img 
                src={require('../assets/images/logo.png')} 
                alt="Pronounceit Logo"
              />
            </Link>
          </div>
        </div>
      </header>

      <div className="profile-content">
        <div className="profile-card">
          <div className="profile-header-section">
            <h1>User Profile</h1>
            {isEditing && (
              <div className="edit-controls">
                <button className="save-button" onClick={handleSubmit}>
                  <FontAwesomeIcon icon={faSave} /> Save
                </button>
                <button className="cancel-button" onClick={cancelEdit}>
                  <FontAwesomeIcon icon={faTimes} /> Cancel
                </button>
              </div>
            )}
          </div>

          <div className="profile-avatar-section">
            <div className="profile-avatar">
              <span>{userData.username.charAt(0)}</span>
            </div>
          </div>

          <div className="profile-details">
            <div className="detail-item">
              <div className="detail-icon">
                <FontAwesomeIcon icon={faUser} />
              </div>
              <div className="detail-content">
                <h3>Username</h3>
                {isEditing ? (
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    className="edit-in-place"
                  />
                ) : (
                  <p>{userData.username}</p>
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
                <p>{userData.role === "student" ? "Student" : "Teacher"}</p>
              </div>
            </div>

            {/* Password fields only show in edit mode */}
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

          <div className="profile-footer">
            {!isEditing ? (
              <>
                <button className="edit-button footer-edit" onClick={() => setIsEditing(true)}>
                  <FontAwesomeIcon icon={faEdit} /> Edit Profile
                </button>
                <Link to="/user-dashboard" className="back-button">
                  Back to Dashboard
                </Link>
              </>
            ) : (
              <Link to="/user-dashboard" className="back-button">
                Back to Dashboard
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserInformation;