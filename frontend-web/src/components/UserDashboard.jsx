import {
  faDownload,
  faSignOutAlt,
  faUser
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/AppPromo.css"; // New CSS file for the promotional content
import "../assets/css/Dashboard.css";
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
      top: `${rect.bottom + 3}px`, // Reduced from 5px for tighter spacing
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
      {/* Keep the header intact */}
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
            </div>
          </div>
        </div>
      </header>

      {/* New content area for app promotion */}
      <div className="app-promo-container">
        <div className="app-promo-content">
          <div className="app-promo-text">
            <h1>Get PronounceIT App Now!</h1>
            <p className="app-description">
              Take your pronunciation practice anywhere with our mobile app. Perfect for kids to learn on the go!
            </p>
            
            <div className="app-features">
              <div className="feature-item">
                <div className="feature-icon">🎮</div>
                <div className="feature-text">
                  <h3>Fun Games</h3>
                  <p>Interactive games that make learning pronunciation enjoyable</p>
                </div>
              </div>
              
              <div className="feature-item">
                <div className="feature-icon">🏆</div>
                <div className="feature-text">
                  <h3>Track Progress</h3>
                  <p>Monitor improvement and celebrate achievements</p>
                </div>
              </div>
              
              <div className="feature-item">
                <div className="feature-icon">🔊</div>
                <div className="feature-text">
                  <h3>Voice Recognition</h3>
                  <p>Advanced technology to help perfect pronunciation</p>
                </div>
              </div>
            </div>
            
            <div className="app-buttons">
              <a 
                href="https://drive.google.com/file/d/16rKD6D6HjUkqAm33z03bHZBVezKTKfKX/view?fbclid=IwY2xjawKhtLZleHRuA2FlbQIxMQABHq62w6Xx3MeKfVZb0wCRgClzVuz4uNxs5zaMnTtwSAE6vndD3aC-0HCG0Uwo_aem_u6im9xfivCshzn0oPvEdIQ" 
                className="google-play-btn"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon icon={faDownload} />
                Download for Android
              </a>
            </div>
          </div>
          
          <div className="app-promo-image">
            <img src={require("../assets/images/logo.png")} alt="PronounceIT App Preview" />
            <div className="app-qr-code">
              <img src={require("../assets/images/PronounceIT.png")} alt="QR Code to download app" />
              <p>Scan to download</p>
            </div>
          </div>
        </div>
      </div>

      {renderDropdown()}
    </div>
  );
};

export default UserDashboard;