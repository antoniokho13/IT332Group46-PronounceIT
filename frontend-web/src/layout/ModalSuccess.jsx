import { faCheck, faTimes } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "../assets/css/ModalSuccess.css";
import adminIcon from "../assets/images/adminicon.png";
import studentIcon from "../assets/images/studenticon.png";
import teacherIcon from "../assets/images/teachericon.png";

const ModalSuccess = ({ show, message, type, role, onClose, redirectPath, actionType }) => {
  if (!show) return null; // Don't render anything if not shown

  // Base classes (kept same for style consistency)
  const overlayClass = "modal-success-notification-overlay";
  const modalBaseClass = "modal-success-notification-modal";
  const iconBaseClass = "modal-success-notification-icon";
  const iconImgClass = "modal-success-admin-icon-img";
  const contentClass = "modal-success-notification-content";
  const buttonClass = "modal-success-notification-button";

  // Dynamic class assignments
  const modalClass = `${modalBaseClass} ${type}`;
  const iconClass = `${iconBaseClass} ${role ? "has-image" : ""}`;

  /**
   * 🔹 Determine what to show inside the circle:
   * - If editing, deleting, or adding → use check or X icons
   * - Otherwise (login success) → show role icon image
   */
  const renderIcon = () => {
    // ✅ SUCCESS CASE
    if (type === "success") {
      // For add/edit/delete actions — always use check icon
      if (["add", "edit", "delete"].includes(actionType)) {
        return (
          <FontAwesomeIcon
            icon={faCheck}
            style={{
              fontSize: "40px",
              color: "#fff",
              backgroundColor: "#58cc83",
              borderRadius: "50%",
              padding: "18px",
              boxShadow: "0 3px 10px rgba(0,0,0,0.15)",
            }}
          />
        );
      }

      // For role-based success (like login)
      if (role === "ADMIN") {
        return <img src={adminIcon} alt="Admin Icon" className={iconImgClass} />;
      } else if (role === "TEACHER") {
        return <img src={teacherIcon} alt="Teacher Icon" className={iconImgClass} />;
      } else if (role === "STUDENT") {
        return <img src={studentIcon} alt="Student Icon" className={iconImgClass} />;
      } else {
        // Default success fallback
        return (
          <FontAwesomeIcon
            icon={faCheck}
            style={{
              fontSize: "40px",
              color: "#fff",
              backgroundColor: "#58cc83",
              borderRadius: "50%",
              padding: "18px",
              boxShadow: "0 3px 10px rgba(0,0,0,0.15)",
            }}
          />
        );
      }
    }

    // ❌ ERROR CASE (always red X)
    else if (type === "error") {
      return (
        <FontAwesomeIcon
          icon={faTimes}
          style={{
            fontSize: "40px",
            color: "#fff",
            backgroundColor: "#ef4444",
            borderRadius: "50%",
            padding: "18px",
            boxShadow: "0 3px 10px rgba(0,0,0,0.15)",
          }}
        />
      );
    }

    // ℹ️ INFO (optional)
    else {
      return "ℹ";
    }
  };

  return (
    <div className={overlayClass}>
      <div className={modalClass}>
        <div className={iconClass}>{renderIcon()}</div>
        <div className={contentClass}>
          <p>{message}</p>
          <button onClick={onClose} className={buttonClass}>
            {redirectPath ? "Continue" : "Close"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ModalSuccess;
