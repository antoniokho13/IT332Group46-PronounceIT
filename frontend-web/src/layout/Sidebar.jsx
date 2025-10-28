import {
  faBookOpen,
  faChartLine,
  faFolder,
  faTrophy,
  faUsers,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "../assets/css/Dashboard.css";
import "../assets/css/Sidebar.css";

/**
 * Sidebar Component
 * -----------------
 * Used by both Teacher and Admin dashboards.
 * - Teacher: shows Lessons, Categories, Analytics.
 * - Admin (AchievementManagement, UserManagement): shows Achievements, Users.
 */
const Sidebar = ({
  activeSection,
  handleNavClick = () => {},
  sidebarOpen = false,
  onAddButtonClick = () => {},
  isWordsPage = false,
  customItems = null, // if this exists, we know it's for Admin
  user = { role: "ADMIN" }, // added for role-based condition
  children,
}) => {
  // 🔹 Determine Add Button Text
  const getButtonText = () => {
    if (customItems && activeSection === "achievements") return "Add Badge";
    if (customItems && activeSection === "users" && user.role === "ADMIN")
      return "Add User";
    if (isWordsPage) return "Add Word";
    switch (activeSection) {
      case "lessons":
        return "Add Lessons";
      case "categories":
        return "Add Category";
      case "analytics":
        return "Student Analytics";
      default:
        return "";
    }
  };

  // 🔹 Disable Add button on analytics or users (for admin if not allowed)
  const isButtonDisabled =
    (activeSection === "analytics" && !isWordsPage && !customItems) ||
    (customItems && activeSection === "users" && user.role !== "ADMIN");

  // 🔹 Icon selection based on section name
  const getIconForItem = (key) => {
    switch (key) {
      case "achievements":
        return faTrophy;
      case "users":
        return faUsers;
      case "lessons":
        return faBookOpen;
      case "categories":
        return faFolder;
      case "analytics":
        return faChartLine;
      default:
        return faFolder;
    }
  };

  // 🔹 Render Navigation
  const renderNavItems = () => {
    if (customItems && Array.isArray(customItems)) {
      return customItems.map((item) => (
        <li
          key={item.key}
          className={activeSection === item.key ? "active" : ""}
          onClick={() => handleNavClick(item.key)}
        >
          <FontAwesomeIcon
            icon={getIconForItem(item.key)}
            className="sidebar-icon"
          />
          {item.label}
        </li>
      ));
    }

    // === Default Teacher Sidebar ===
    return (
      <>
        <li
          className={activeSection === "lessons" ? "active" : ""}
          onClick={() => handleNavClick("lessons")}
        >
          <FontAwesomeIcon icon={faBookOpen} className="sidebar-icon" />
          Lessons
        </li>
        <li
          className={activeSection === "categories" ? "active" : ""}
          onClick={() => handleNavClick("categories")}
        >
          <FontAwesomeIcon icon={faFolder} className="sidebar-icon" />
          Categories
        </li>
        <li
          className={activeSection === "analytics" ? "active" : ""}
          onClick={() => handleNavClick("analytics")}
        >
          <FontAwesomeIcon icon={faChartLine} className="sidebar-icon" />
          Student Analytics
        </li>
      </>
    );
  };

  // 🔹 Determine if Add Button should show
  const showAddButton = (() => {
    // For Admin — show only on achievements or users
    if (customItems && user.role === "ADMIN") {
      return activeSection === "achievements" || activeSection === "users";
    }
    // For teachers or other pages
    if (!customItems && activeSection !== "analytics") return true;
    return false;
  })();

  return (
    <div className="dashboard single">
      {/* ===== Sidebar Navigation ===== */}
      <aside className={`sidebar ${sidebarOpen ? "active" : ""}`}>
        {showAddButton && (
          <button
            className="sidebar-action-button"
            onClick={onAddButtonClick}
            disabled={isButtonDisabled}
          >
            {getButtonText()}
          </button>
        )}

        <nav>
          <ul>{renderNavItems()}</ul>
        </nav>
      </aside>

      {/* ===== MAIN CONTENT ===== */}
      <main className="content">{children}</main>
    </div>
  );
};

export default Sidebar;
