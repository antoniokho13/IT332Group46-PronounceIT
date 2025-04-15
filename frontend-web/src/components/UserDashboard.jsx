import React, { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/Dashboard.css";
import {
  faBookOpen,
  faChartLine,
  faChevronDown,
  faHome,
  faMedal,
  faSignOutAlt,
  faUser,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { logout } from "../services/authService";
import { getUserById } from "../services/userService";

const UserDashboard = () => {
  const [user, setUser] = useState({ firstName: "", lastName: "" });
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (token && storedUser && storedUser.userId) {
          const userData = await getUserById(storedUser.userId, token);
          setUser({ firstName: userData.firstName, lastName: userData.lastName });
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

  const handleLogout = async () => {
    try {
      await logout(); // Call the logout API
    } catch (error) {
      console.error("Logout API failed:", error);
    } finally {
      localStorage.removeItem("user"); // Clear user data from localStorage
      localStorage.removeItem("token"); // Clear token from localStorage
      navigate("/login"); // Redirect to login page
    }
  };

  // Close dropdown when clicking outside
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

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownRef, userCardRef]);

  const toggleDropdown = (e) => {
    e.stopPropagation();
    setShowDropdown(!showDropdown);
  };

  const renderDropdown = () => {
    if (!showDropdown) return null;

    const rect = userCardRef.current?.getBoundingClientRect();
    if (!rect) return null;

    const dropdownStyle = {
      position: "fixed",
      top: `${rect.bottom + 5}px`,
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
            e.stopPropagation(); // Prevent dropdown from closing
            handleLogout();
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
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="container">
          <div className="logo">
            <Link to="/">
              <img
                src={require("../assets/images/logo.png")}
                alt="Pronounceit Logo"
              />
            </Link>
          </div>
          <div className="user-card" ref={userCardRef} onClick={toggleDropdown}>
            <div className="default-avatar">
              <span>{user.firstName.charAt(0)}</span>
            </div>
            <div className="user-info">
              <p>{`${user.firstName} ${user.lastName}`}</p>
              <FontAwesomeIcon icon={faChevronDown} className="dropdown-icon" />
            </div>
          </div>
        </div>
      </header>

      <div className="dashboard single">
        <aside className="sidebar">
          <nav>
            <ul>
              <li className="active">
                <FontAwesomeIcon icon={faHome} className="sidebar-icon" />
                Dashboard
              </li>
              <li>
                <FontAwesomeIcon icon={faChartLine} className="sidebar-icon" />
                My Progress
              </li>
              <li>
                <FontAwesomeIcon icon={faBookOpen} className="sidebar-icon" />
                Lessons
              </li>
              <li>
                <FontAwesomeIcon icon={faMedal} className="sidebar-icon" />
                Achievements
              </li>
            </ul>
          </nav>
        </aside>

        <main className="content">
          <h2 className="dashboard-title">Student Dashboard</h2>

          <div className="metrics">
            <div className="metric">
              <p>Completed Lessons</p>
              <h4>24</h4>
            </div>
            <div className="metric">
              <p>Stars Earned</p>
              <h4>152</h4>
            </div>
            <div className="metric">
              <p>Practice Hours</p>
              <h4>18.5</h4>
            </div>
          </div>

          <div className="marketplace">
            <div className="card analytics">
              <p>Pronunciation Overview</p>
              <h4>75% Accuracy</h4>
              <button>START PRACTICE</button>
            </div>
            <div className="card flow">
              <p>Daily Streak</p>
              <h4>7 Days</h4>
              <p>Keep it up!</p>
            </div>
            <div className="card upgrade">
              <p>Achievements</p>
              <h4>3 Badges Earned</h4>
              <p>View your learning milestones</p>
            </div>
          </div>

          <div className="orders">
            <h3>Recent Activities</h3>
            <ul>
              <li>Completed "Vowel Sounds" lesson — 15 stars earned</li>
              <li>Played "Word Match" game — 8 stars earned</li>
              <li>Completed daily practice — 5 stars earned</li>
              <li>Finished "Consonant Blends" quiz — 12 stars earned</li>
            </ul>
          </div>

          <div className="report-progress">
            <h3>Progress Report</h3>

            <div className="graph-container animate-bars">
              <div className="grid-lines">
                <div className="grid-line" data-percentage="100%"></div>
                <div className="grid-line" data-percentage="75%"></div>
                <div className="grid-line" data-percentage="50%"></div>
                <div className="grid-line" data-percentage="25%"></div>
                <div className="grid-line" data-percentage="0%"></div>
              </div>
              <div className="chart-bar beginner">
                <div className="chart-bar-percentage">75%</div>
                <div className="chart-bar-label">Beginner</div>
              </div>
              <div className="chart-bar intermediate">
                <div className="chart-bar-percentage">40%</div>
                <div className="chart-bar-label">Intermediate</div>
              </div>
              <div className="chart-bar advanced">
                <div className="chart-bar-percentage">15%</div>
                <div className="chart-bar-label">Advanced</div>
              </div>
            </div>
          </div>
        </main>
      </div>

      {renderDropdown()}
    </div>
  );
};

export default UserDashboard;