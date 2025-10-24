import '../assets/css/ModalSuccess.css'; // Ensure this path is correct
import adminIcon from '../assets/images/adminicon.png';
import studentIcon from '../assets/images/studenticon.png';
import teacherIcon from '../assets/images/teachericon.png';

const ModalSuccess = ({ show, message, type, role, onClose, redirectPath }) => {
  if (!show) {
    return null; // Don't render anything if show is false
  }

  // Define base class names with prefix
  const overlayClass = "modal-success-notification-overlay";
  const modalBaseClass = "modal-success-notification-modal";
  const iconBaseClass = "modal-success-notification-icon";
  const iconImgClass = "modal-success-admin-icon-img"; // Using a distinct name for image class
  const contentClass = "modal-success-notification-content";
  const buttonClass = "modal-success-notification-button";

  // Construct dynamic class names
  const modalClass = `${modalBaseClass} ${type}`; // e.g., "modal-success-notification-modal success"
  const iconClass = `${iconBaseClass} ${role ? 'has-image' : ''}`; // e.g., "modal-success-notification-icon has-image"

  return (
    <div className={overlayClass}>
      <div className={modalClass}>
        <div className={iconClass}>
          {type === 'success' ? (
            role === 'ADMIN' ? (
              <img src={adminIcon} alt="Admin Icon" className={iconImgClass} />
            ) : role === 'TEACHER' ? (
              <img src={teacherIcon} alt="Teacher Icon" className={iconImgClass} />
            ) : role === 'STUDENT' ? (
              <img src={studentIcon} alt="Student Icon" className={iconImgClass} />
            ) : (
              '✓' // Default success icon if no role or unknown role
            )
          ) : type === 'error' ? (
            '✗' // Error icon
          ) : (
            'ℹ' // Info icon (if you ever add an 'info' type)
          )}
        </div>
        <div className={contentClass}>
          <p>{message}</p>
          <button onClick={onClose} className={buttonClass}>
            {/* Change button text based on whether it redirects */}
            {redirectPath ? 'Continue' : 'Close'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ModalSuccess;