import React, { useEffect, useRef, useState } from "react";
import ReactDOM from 'react-dom'; // Add this import
import { Link } from "react-router-dom";
import "../assets/css/Dashboard.css";
// Import Font Awesome components
import {
  faBookOpen,
  faChartLine,
  faChevronDown,
  faHome,
  faMedal,
  faSignOutAlt,
  faUser
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const UserDashboard = () => {
  // State to toggle dropdown visibility
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null); // Add this ref for positioning

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target) &&
          userCardRef.current && !userCardRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownRef, userCardRef]);

  // Toggle dropdown function
  const toggleDropdown = (e) => {
    e.stopPropagation(); // Prevent event bubbling
    setShowDropdown(!showDropdown);
  };

  // Create a portal for the dropdown menu
  const renderDropdown = () => {
    if (!showDropdown) return null;
    
    // Get position of user-card for dropdown placement
    const rect = userCardRef.current?.getBoundingClientRect();
    
    if (!rect) return null;
    
    const dropdownStyle = {
      position: 'fixed',
      top: `${rect.bottom + 5}px`,
      right: `${window.innerWidth - rect.right}px`,
      background: 'white',
      borderRadius: '8px',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      zIndex: 9999,
      width: '200px',
      padding: '10px 0',
    };
    
    return ReactDOM.createPortal(
      <div 
        className="user-dropdown-portal" 
        style={dropdownStyle}
        ref={dropdownRef}
      >
        <Link to="/profile" className="dropdown-item">
          <FontAwesomeIcon icon={faUser} className="dropdown-icon" />
          Edit Profile
        </Link>
        <Link to="/logout" className="dropdown-item">
          <FontAwesomeIcon icon={faSignOutAlt} className="dropdown-icon" />
          Logout
        </Link>
      </div>,
      document.body
    );
  };

  return (
    <div className="dashboard-container">
      {/* Header with logo and user profile */}
      <header className="dashboard-header">
        <div className="container">
          <div className="logo">
            <Link to="/">
              <img 
                src={require('../assets/images/logo.png')} 
                alt="Pronounceit Logo"
              />
            </Link>
          </div>
          {/* User profile in header with dropdown */}
          <div className="user-card" ref={userCardRef} onClick={toggleDropdown}>
            <div className="default-avatar">
              <span>M</span>
            </div>
            <div className="user-info">
              <p>Melgod</p>
              <FontAwesomeIcon icon={faChevronDown} className="dropdown-icon" />
            </div>
          </div>
        </div>
      </header>

      {/* Rest of the dashboard */}
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
              {/* Grid lines */}
              <div className="grid-lines">
                <div className="grid-line" data-percentage="100%"></div>
                <div className="grid-line" data-percentage="75%"></div>
                <div className="grid-line" data-percentage="50%"></div>
                <div className="grid-line" data-percentage="25%"></div>
                <div className="grid-line" data-percentage="0%"></div>
              </div>
              
              {/* Bars */}
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
      
      {/* Render dropdown outside the main container */}
      {renderDropdown()}
    </div>
  );
};

export default UserDashboard;
