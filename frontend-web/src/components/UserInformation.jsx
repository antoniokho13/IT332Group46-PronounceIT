import {
  faArrowLeft,
  faEdit,
  faSave,
  faTimes,
  faTrash,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/UserInformation.css";
import studentIcon from "../assets/images/studenticon.png";
import teacherIcon from "../assets/images/teachericon.png";
import ModalSuccess from "../layout/ModalSuccess"; // ✅ Import modal component
import { deleteUser, getUserById, updateUser } from "../services/userService";

const UserInformation = () => {
  const [userData, setUserData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    role: "",
  });
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [isEditing, setIsEditing] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [saveError, setSaveError] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [dataLoaded, setDataLoaded] = useState(false);

  // ✅ Modal States
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("success");
  const [modalMessage, setModalMessage] = useState("");
  const [modalAction, setModalAction] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      if (dataLoaded) return;

      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));

        if (token && storedUser && storedUser.userId) {
          const user =
            storedUser.firstName && storedUser.email
              ? storedUser
              : await getUserById(storedUser.userId, token);

          setUserData(user);
          setFormData({ ...user, password: "", confirmPassword: "" });
          setDataLoaded(true);
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
  }, [navigate, dataLoaded]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (name === "password" || name === "confirmPassword")
      setPasswordError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.password && formData.password !== formData.confirmPassword)
      return setPasswordError("Passwords do not match");
    if (formData.password && formData.password.length < 6)
      return setPasswordError("Password must be at least 6 characters");

    try {
      const token = localStorage.getItem("token");
      const { password, confirmPassword, ...dataToUpdate } = formData;
      const updatedUser = await updateUser(
        userData.id,
        { ...dataToUpdate, password },
        token
      );
      setUserData(updatedUser);
      setFormData({ ...updatedUser, password: "", confirmPassword: "" });
      setIsEditing(false);
      setSaveError("");

      // ✅ Show success modal
      setModalType("success");
      setModalMessage("Profile updated successfully!");
      setModalAction("edit");
      setShowModal(true);
    } catch (error) {
      setSaveError("Failed to save changes. Please try again.");
      setModalType("error");
      setModalMessage("Failed to update profile. Try again.");
      setModalAction("edit");
      setShowModal(true);
    }
  };

  const cancelEdit = () => {
    setFormData({ ...userData, password: "", confirmPassword: "" });
    setPasswordError("");
    setSaveError("");
    setIsEditing(false);
  };

  const handleDeleteAccount = async () => {
    try {
      const token = localStorage.getItem("token");
      await deleteUser(userData.id, token);
      localStorage.removeItem("user");
      localStorage.removeItem("token");

      // ✅ Show delete success modal
      setModalType("success");
      setModalMessage("Account deleted successfully!");
      setModalAction("delete");
      setShowModal(true);

      // Smooth redirect after a short delay
      setTimeout(() => navigate("/"), 2000);
    } catch (error) {
      console.error("Failed to delete user account:", error);
      setModalType("error");
      setModalMessage("Failed to delete account. Try again.");
      setModalAction("delete");
      setShowModal(true);
      setShowDeleteModal(false);
    }
  };

  const getRoleIcon = () => {
    if (userData.role === "STUDENT" || userData.role === "USER")
      return studentIcon;
    if (userData.role === "TEACHER" || userData.role === "ADMIN")
      return teacherIcon;
    return null;
  };

  const getRoleDisplayName = (role) => {
    if (role === "STUDENT" || role === "USER") return "Student";
    if (role === "TEACHER" || role === "ADMIN") return "Teacher";
    return "Unknown";
  };

  if (!userData.firstName)
    return <div className="loading-container">Loading...</div>;

  return (
    <div className="userinfo-wrapper">
      {/* LEFT PANEL */}
      <div className="userinfo-form-side">
        <div className="userinfo-box">
          <Link
            to={
              userData.role === "ADMIN"
                ? "/teacher-dashboard"
                : "/user-dashboard"
            }
            className="userinfo-back"
          >
            <FontAwesomeIcon icon={faArrowLeft} /> Back
          </Link>

          <h2>User Information</h2>

          <form onSubmit={handleSubmit} className="userinfo-form">
            <div>
              <label className="userinfo-label">First Name</label>
              <input
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className="userinfo-input"
                disabled={!isEditing}
              />
            </div>

            <div>
              <label className="userinfo-label">Last Name</label>
              <input
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className="userinfo-input"
                disabled={!isEditing}
              />
            </div>

            <div>
              <label className="userinfo-label">Email</label>
              <input
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="userinfo-input"
                disabled={!isEditing}
              />
            </div>

            <div>
              <label className="userinfo-label">Account Type</label>
              <input
                value={getRoleDisplayName(userData.role)}
                className="userinfo-input"
                disabled
              />
            </div>

            {isEditing && (
              <>
                <div className="userinfo-span-2">
                  <label className="userinfo-label">New Password</label>
                  <input
                    type="password"
                    name="password"
                    placeholder="Enter new password"
                    value={formData.password}
                    onChange={handleChange}
                    className="userinfo-input"
                  />
                </div>

                <div className="userinfo-span-2">
                  <label className="userinfo-label">Confirm Password</label>
                  <input
                    type="password"
                    name="confirmPassword"
                    placeholder="Confirm new password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className="userinfo-input"
                  />
                  {passwordError && (
                    <p className="userinfo-error">{passwordError}</p>
                  )}
                </div>
              </>
            )}

            {isEditing && (
              <div className="userinfo-form-actions userinfo-span-2">
                <button type="submit" className="userinfo-save-btn">
                  <FontAwesomeIcon icon={faSave} /> Save
                </button>
                <button
                  type="button"
                  onClick={cancelEdit}
                  className="userinfo-cancel-btn"
                >
                  <FontAwesomeIcon icon={faTimes} /> Cancel
                </button>
              </div>
            )}
          </form>
        </div>
      </div>

      {/* RIGHT PANEL */}
      <div className="userinfo-gradient-side">
        <div className="userinfo-gradient-content">
          <h2>User Profile</h2>
          <p>View your account details</p>

          <div className="userinfo-role-section">
            <img
              src={getRoleIcon()}
              alt="Role Icon"
              className="userinfo-role-icon"
            />
            <div className="userinfo-role-info">
              <h3>
                {userData.firstName} {userData.lastName}
              </h3>
              <p>{getRoleDisplayName(userData.role)}</p>
            </div>
          </div>

          {!isEditing && (
            <div className="userinfo-actions">
              <button
                onClick={() => setIsEditing(true)}
                className="userinfo-btn userinfo-edit-btn"
              >
                <FontAwesomeIcon icon={faEdit} /> Edit
              </button>
              <button
                onClick={() => setShowDeleteModal(true)}
                className="userinfo-btn userinfo-delete-btn"
              >
                <FontAwesomeIcon icon={faTrash} /> Delete
              </button>
            </div>
          )}
        </div>
      </div>

      {/* DELETE CONFIRMATION MODAL */}
      {showDeleteModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Confirm Account Deletion</h3>
            <p>
              Are you sure you want to delete your account? This action cannot
              be undone and all your data will be permanently removed.
            </p>
            <div className="modal-actions">
              <button
                onClick={handleDeleteAccount}
                className="confirm-delete-btn"
              >
                <FontAwesomeIcon icon={faTrash} /> Yes, Delete Account
              </button>
              <button
                onClick={() => setShowDeleteModal(false)}
                className="cancel-modal-btn"
              >
                <FontAwesomeIcon icon={faTimes} /> Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ✅ Notification Modal */}
      <ModalSuccess
        show={showModal}
        type={modalType}
        message={modalMessage}
        actionType={modalAction}
        onClose={() => setShowModal(false)}
      />
    </div>
  );
};

export default UserInformation;
