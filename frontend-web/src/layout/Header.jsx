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

  // ✅ Smooth scroll for homepage navigation
  const handleNavClick = (e) => {
    e.preventDefault();
    const href = e.currentTarget.getAttribute("href");

    if (href && href.startsWith("#")) {
      const isHomePage = window.location.pathname === "/";
      const targetId = href.substring(1);

      if (isHomePage) {
        const targetElement = document.querySelector(href);
        if (targetElement) {
          const headerOffset = 80;
          const elementPosition = targetElement.getBoundingClientRect().top;
          const offsetPosition =
            elementPosition + window.pageYOffset - headerOffset;

          const start = window.scrollY;
          const distance = offsetPosition - start;
          const duration = 800;
          let startTime = null;

          const easeInOutCubic = (t) =>
            t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;

          const animateScroll = (currentTime) => {
            if (startTime === null) startTime = currentTime;
            const timeElapsed = currentTime - startTime;
            const progress = Math.min(timeElapsed / duration, 1);
            const easedProgress = easeInOutCubic(progress);

            window.scrollTo(0, start + distance * easedProgress);

            if (timeElapsed < duration) {
              requestAnimationFrame(animateScroll);
            }
          };

          requestAnimationFrame(animateScroll);
        }

        if (closeMenu) setTimeout(closeMenu, 100);
      } else {
        window.location.href = `/${href}`;
      }
    }
  };

  // ✅ Dropdown rendering logic
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

    const isAdminPage =
      pageTitle === "User Management" || pageTitle === "Achievement Management";

    return ReactDOM.createPortal(
      <div
        className="user-dropdown-portal"
        style={dropdownStyle}
        ref={dropdownRef}
      >
        {/* ✅ Hide Edit Profile for Admin, keep only Logout */}
        {!isAdminPage && (
          <Link to="/profile" className="dropdown-item">
            <FontAwesomeIcon icon={faUser} className="dropdown-icon" />
            Edit Profile
          </Link>
        )}

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

  const displayName = user?.firstName
    ? `${user.firstName} ${user.lastName}`
    : "User";

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

          {isDashboard && (
            <div className="dashboard-page-title">
              <h2>{pageTitle}</h2>
            </div>
          )}

          {!isDashboard && (
            <nav className={menuOpen ? "active" : ""}>
              <ul>
                <li>
                  <a href="#features" onClick={handleNavClick}>
                    Features
                  </a>
                </li>
                <li>
                  <a href="#how-it-works" onClick={handleNavClick}>
                    How It Works
                  </a>
                </li>
                <li>
                  <a href="#team" onClick={handleNavClick}>
                    Developers
                  </a>
                </li>
                <li>
                  <a href="#testimonials" onClick={handleNavClick}>
                    Testimonials
                  </a>
                </li>
                <li>
                  <a href="#faq" onClick={handleNavClick}>
                    FAQ
                  </a>
                </li>
              </ul>
              <div className="mobile-buttons">
                <Link
                  to="/login"
                  className="btn btn-secondary"
                  onClick={closeMenu}
                >
                  Log In
                </Link>
                <Link
                  to="/login?signup=true"
                  className="btn btn-primary"
                  onClick={closeMenu}
                >
                  Sign Up
                </Link>
              </div>
            </nav>
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
                    <p>{displayName}</p>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </header>

      {/* ✅ Dropdown shows Logout only on admin side */}
      {isDashboard && renderDropdown()}
    </>
  );
}

export default Header;
