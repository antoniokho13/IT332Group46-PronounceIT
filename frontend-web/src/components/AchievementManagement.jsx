import {
  faAward,
  faEdit,
  faTrash
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useNavigate } from "react-router-dom";

// ✅ Use your unified dashboard styles
import "../assets/css/Dashboard.css";

// ✅ NEW: Import Achievement-specific modal styles
import "../assets/css/AchievementModal.css";
import "../assets/css/ModalSuccess.css";
import ModalSuccess from "../layout/ModalSuccess";

import Header from "../layout/Header";
import Sidebar from "../layout/Sidebar";

import {
  createAchievement,
  deleteAchievement,
  getAllAchievements,
  updateAchievement,
} from "../services/achievementService";
import { logout } from "../services/authService";

const AchievementManagement = () => {
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);

  // ✅ Pagination states
  const [page, setPage] = useState(1);
  const itemsPerPage = 5;

  // ✅ ModalSuccess states
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successModalData, setSuccessModalData] = useState({
    message: "",
    type: "",
  });

  const modalRef = useRef(null);
  const deleteModalRef = useRef(null);
  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  const user = { firstName: "Admin", lastName: "User" };

  // === Load all achievements ===
  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await getAllAchievements();
        setAchievements(data);
      } catch (error) {
        console.error("Error fetching achievements:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // === Refresh ===
  const refreshAchievements = async () => {
    try {
      const data = await getAllAchievements();
      setAchievements(data);
    } catch (error) {
      console.error("Error refreshing:", error);
    }
  };

  // === Logout ===
  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  // === Sidebar Navigation ===
  const handleNavClick = (section) => {
    if (section === "users") navigate("/user-management");
  };

  // === Add/Edit Achievement ===
  const handleAddAchievement = async (e) => {
    e.preventDefault();
    try {
      const name = e.target.name.value;
      const description = e.target.description.value;
      const pointsRequired = parseInt(e.target.pointsRequired.value, 10);
      const isActive = e.target.isActive.value === "true";
      const badgeFile = e.target.badgeImage.files[0];

      const data = { name, description, pointsRequired, isActive, badgeFile };

      if (editingItem) {
        await updateAchievement(editingItem.id, data);
        setSuccessModalData({
          message: "Achievement updated successfully!",
          type: "success",
        });
      } else {
        await createAchievement(data);
        setSuccessModalData({
          message: "Achievement added successfully!",
          type: "success",
        });
      }

      setShowSuccessModal(true);
      await refreshAchievements();
      setShowModal(false);
      setEditingItem(null);
      setPreviewImage(null);
    } catch (error) {
      console.error("Error saving achievement:", error);
      setSuccessModalData({
        message: "Failed to save achievement.",
        type: "error",
      });
      setShowSuccessModal(true);
    }
  };

  // === Image Preview ===
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => setPreviewImage(reader.result);
      reader.readAsDataURL(file);
    }
  };

  // === Modal Control ===
  const openModal = (item = null) => {
    setEditingItem(item);
    setShowModal(true);
    if (item && item.badgeUrl) setPreviewImage(item.badgeUrl);
    else setPreviewImage(null);
  };

  const handleDelete = (achievement) => {
    setItemToDelete(achievement);
    setShowDeleteModal(true);
  };

  const confirmDelete = async () => {
    try {
      await deleteAchievement(itemToDelete.id);
      await refreshAchievements();
      setShowDeleteModal(false);
      setSuccessModalData({
        message: "Achievement deleted successfully!",
        type: "success",
      });
      setShowSuccessModal(true);
    } catch (error) {
      setSuccessModalData({
        message: "Failed to delete achievement.",
        type: "error",
      });
      setShowSuccessModal(true);
    }
  };

  // === Pagination Logic ===
  const totalPages = Math.ceil(achievements.length / itemsPerPage);
  const startIndex = (page - 1) * itemsPerPage;
  const currentItems = achievements.slice(startIndex, startIndex + itemsPerPage);
  const pageNumbers = [...Array(totalPages).keys()].map((num) => num + 1);

  // === Render Achievement Table ===
  const renderAchievementsTable = () => {
    if (loading)
      return (
        <tr>
          <td colSpan="6" style={{ textAlign: "center" }}>
            Loading...
          </td>
        </tr>
      );

    if (achievements.length === 0)
      return (
        <tr>
          <td colSpan="6" style={{ textAlign: "center" }}>
            No achievements found.
          </td>
        </tr>
      );

    return currentItems.map((achievement) => (
      <tr key={achievement.id}>
        <td>
          {achievement.badgeUrl ? (
            <img
              src={achievement.badgeUrl}
              alt="Badge"
              style={{
                width: "40px",
                height: "40px",
                borderRadius: "50%",
                objectFit: "cover",
              }}
            />
          ) : (
            <FontAwesomeIcon icon={faAward} />
          )}
        </td>
        <td>{achievement.name}</td>
        <td>{achievement.description}</td>
        <td>{achievement.pointsRequired}</td>
        <td>{achievement.isActive ? "Active" : "Inactive"}</td>
        <td className="action-buttons-cell">
          <button
            className="action-btn"
            title="Edit"
            onClick={() => openModal(achievement)}
          >
            <FontAwesomeIcon icon={faEdit} />
          </button>
          <button
            className="action-btn delete"
            title="Delete"
            onClick={() => handleDelete(achievement)}
          >
            <FontAwesomeIcon icon={faTrash} />
          </button>
        </td>
      </tr>
    ));
  };

  // === Modals with NEW class names ===
  const renderModal = () => {
    if (!showModal) return null;
    const isEditing = editingItem !== null;

    return ReactDOM.createPortal(
      <div className="achievement-modal-overlay">
        <div className="achievement-modal-container" ref={modalRef}>
          <h3>{isEditing ? "Edit Achievement" : "Add New Achievement"}</h3>
          <form className="achievement-modal-form" onSubmit={handleAddAchievement}>
            <div className="achievement-form-group">
              <label>Name</label>
              <input
                id="name"
                name="name"
                defaultValue={isEditing ? editingItem.name : ""}
                required
              />
            </div>
            <div className="achievement-form-group">
              <label>Description</label>
              <textarea
                id="description"
                name="description"
                defaultValue={isEditing ? editingItem.description : ""}
                required
              />
            </div>
            <div className="achievement-form-group">
              <label>Badge Image</label>
              {previewImage && (
                <div className="achievement-image-preview">
                  <img
                    src={previewImage}
                    alt="Preview"
                  />
                </div>
              )}
              <input
                type="file"
                id="badgeImage"
                name="badgeImage"
                accept="image/*"
                ref={fileInputRef}
                onChange={handleImageChange}
              />
            </div>
            <div className="achievement-form-group">
              <label>Points Required</label>
              <input
                id="pointsRequired"
                name="pointsRequired"
                type="number"
                defaultValue={isEditing ? editingItem.pointsRequired : 100}
              />
            </div>
            <div className="achievement-form-group">
              <label>Status</label>
              <select
                id="isActive"
                name="isActive"
                defaultValue={
                  isEditing && !editingItem.isActive ? "false" : "true"
                }
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>
            <div className="achievement-modal-actions">
              <button
                type="button"
                className="achievement-cancel-btn"
                onClick={() => setShowModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="achievement-submit-btn">
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
      <div className="achievement-modal-overlay">
        <div className="achievement-delete-modal-container" ref={deleteModalRef}>
          <h3>Confirm Delete</h3>
          <p>Are you sure you want to delete "<strong>{itemToDelete.name}</strong>"? This action cannot be undone.</p>
          <div className="achievement-modal-actions">
            <button
              className="achievement-cancel-btn"
              onClick={() => setShowDeleteModal(false)}
            >
              Cancel
            </button>
            <button className="achievement-delete-btn" onClick={confirmDelete}>
              Delete
            </button>
          </div>
        </div>
      </div>,
      document.body
    );
  };

  return (
    <>
      <Header
        isDashboard={true}
        user={user}
        onLogout={handleLogout}
        toggleSidebar={() => setSidebarOpen(!sidebarOpen)}
        sidebarOpen={sidebarOpen}
        pageTitle="Achievement Management"
      />

      <Sidebar
        activeSection="achievements"
        handleNavClick={handleNavClick}
        sidebarOpen={sidebarOpen}
        onAddButtonClick={() => openModal(null)}
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
                  <th>Badge</th>
                  <th>Name</th>
                  <th>Description</th>
                  <th>Points</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>{renderAchievementsTable()}</tbody>
            </table>

            {/* Pagination */}
            <div className="pagination-container right">
              {pageNumbers.map((num) => (
                <button
                  key={num}
                  className={`pagination-number ${
                    page === num ? "active" : ""
                  }`}
                  onClick={() => setPage(num)}
                >
                  {num}
                </button>
              ))}
            </div>
          </div>
        </div>
      </Sidebar>

      {renderModal()}
      {renderDeleteModal()}

      {/* ✅ Success modal integration */}
      <ModalSuccess
        show={showSuccessModal}
        message={successModalData.message}
        type={successModalData.type}
        role="success"
        onClose={() => setShowSuccessModal(false)}
      />
    </>
  );
};

export default AchievementManagement;