import { faDownload } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../assets/css/AppPromo.css";
import scanImage from "../assets/images/scan.png";
import Header from "../layout/Header";
import { logout } from "../services/authService";
import { getUserById } from "../services/userService";

const UserDashboard = () => {
  const [user, setUser] = useState({ firstName: "", lastName: "" });
  const [dataLoaded, setDataLoaded] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserData = async () => {
      if (dataLoaded) return;
      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (token && storedUser && storedUser.userId) {
          if (storedUser.firstName && storedUser.lastName) {
            setUser({ firstName: storedUser.firstName, lastName: storedUser.lastName });
            setDataLoaded(true);
            return;
          }
          const userData = await getUserById(storedUser.userId, token);
          setUser({ firstName: userData.firstName, lastName: userData.lastName });
          const updatedStoredUser = { ...storedUser, firstName: userData.firstName, lastName: userData.lastName };
          localStorage.setItem("user", JSON.stringify(updatedStoredUser));
          setDataLoaded(true);
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
  }, [navigate, dataLoaded]);

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error("Logout API failed:", error);
    } finally {
      localStorage.removeItem("user");
      localStorage.removeItem("token");
      navigate("/login");
    }
  };

  return (
    <div className="dashboard-container">
      <Header
        isDashboard={true}
        user={user}
        onLogout={handleLogout}
        pageTitle=""
        isStudent={true}
      />

      <div className="app-promo-container">
        <div className="app-promo-content">
          {/* LEFT SIDE TEXT & BUTTON */}
          <div className="app-promo-text">
            <h1>Get PronounceIT App Now!</h1>
            <p>
              Take your pronunciation practice anywhere with our mobile app.
              Perfect for kids to learn on the go!
            </p>

            <div className="button-container">
              <a
                href="https://drive.google.com/file/d/16rKD6D6HjUkqAm33z03bHZBVezKTKfKX/view"
                className="download-now-btn"
                target="_blank"
                rel="noopener noreferrer"
              >
                <FontAwesomeIcon icon={faDownload} /> Download Now
              </a>
            </div>
          </div>

          {/* RIGHT SIDE SCAN IMAGE */}
          <div className="app-promo-image">
            <img src={scanImage} alt="Scan QR to download PronounceIT app" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;
