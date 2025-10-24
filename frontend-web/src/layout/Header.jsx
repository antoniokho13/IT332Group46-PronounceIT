import {
  faBars,
  faSignOutAlt,
  faTimes,
  faUser,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link } from "react-router-dom";
import "../assets/css/Header.css";
import teacherIcon from "../assets/images/teachericon.png";

function Header({
  menuOpen,
  toggleMenu,
  closeMenu,
  isDashboard = false,
  user = null,
  onLogout = () => {},
  toggleSidebar = () => {},
  sidebarOpen = false,
  pageTitle = "",
}) {
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);

  const toggleDropdown = (e) => {
    e.stopPropagation();
    setShowDropdown(!showDropdown);
  };

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

    if (isDashboard) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownRef, userCardRef, isDashboard]);

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
            onLogout();
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

  return (
    <>
      {!isDashboard && (
        <div
          className={`mobile-menu-overlay ${menuOpen ? "active" : ""}`}
          onClick={closeMenu}
        ></div>
      )}

      <header>
        <div className="container">
          <div className="logo">
            <Link to={isDashboard ? "/teacher-dashboard" : "/"}>
              <img
                src={require("../assets/images/logo.png")}
                alt="Pronounceit Logo"
              />
            </Link>
          </div>

          {/* Centered title for dashboard */}
          {isDashboard && (
            <div className="dashboard-page-title">
              <h2>{pageTitle}</h2>
            </div>
          )}

          <div className="header-right">
            {!isDashboard && (
              <>
                <div className="desktop-buttons">
                  <Link
                    to="/login"
                    className="btn btn-secondary"
                    style={{ marginRight: "15px" }}
                  >
                    Log In
                  </Link>
                  <Link to="/login?signup=true" className="btn btn-secondary">
                    Sign Up
                  </Link>
                </div>
                <div className="mobile-menu-button" onClick={toggleMenu}>
                  <FontAwesomeIcon icon={menuOpen ? faTimes : faBars} />
                </div>
              </>
            )}

            {isDashboard && (
              <>
                <button
                  className={`hamburger-menu ${sidebarOpen ? "active" : ""}`}
                  onClick={toggleSidebar}
                  aria-label="Toggle menu"
                >
                  <span></span>
                  <span></span>
                  <span></span>
                </button>

                <div
                  className="user-card"
                  ref={userCardRef}
                  onClick={toggleDropdown}
                >
                  <img
                    src={teacherIcon}
                    alt="Teacher"
                    className="teacher-avatar-icon"
                  />
                  <div className="user-info">
                    <p>{`${user.firstName} ${user.lastName}`}</p>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </header>

      {isDashboard && renderDropdown()}
    </>
  );
}

export default Header;
