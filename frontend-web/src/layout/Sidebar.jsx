// src/layout/Sidebar.jsx
import {
  faBookOpen,
  faChartLine,
  faFolder,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "../assets/css/Sidebar.css";

const SidebarLayout = ({
  activeSection,
  handleNavClick,
  sidebarOpen,
  children,
}) => {
  return (
    <div className="dashboard single">
      {/* Sidebar */}
      <aside className={`sidebar ${sidebarOpen ? "active" : ""}`}>
        <nav>
          <ul>
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
          </ul>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="content">{children}</main>
    </div>
  );
};

export default SidebarLayout;
