import { faEdit, faTrash } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { useLocation, useNavigate, useParams } from "react-router-dom";
// Import layout components
import Header from "../layout/Header";
import ModalSuccess from "../layout/ModalSuccess";
import SidebarLayout from "../layout/Sidebar";

// Import required CSS
import "../assets/css/Dashboard.css";
import "../assets/css/Modal.css"; // ✅ for delete modal consistency
import "../assets/css/Words.css";

// Import services
import { logout } from "../services/authService";
import { getUserById } from "../services/userService";
import {
  createWord,
  deleteWord,
  getWordsByLessonId,
  updateWord,
} from "../services/wordService";

const Words = () => {
  const { lessonId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  // State for page data
  const [lessonName, setLessonName] = useState(
    location.state?.lessonName || ""
  );
  const [words, setWords] = useState([]);
  const [loading, setLoading] = useState(true);

  // State for layout
  const [user, setUser] = useState({ firstName: "", lastName: "", id: null });
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // State for modals and notifications
  const [showModal, setShowModal] = useState(false);
  const [newWord, setNewWord] = useState({ word: "" });
  const [imageFile, setImageFile] = useState(null);
  const [editingWord, setEditingWord] = useState(null);
  const [editImageFile, setEditImageFile] = useState(null);
  const [notification, setNotification] = useState({
    show: false,
    message: "",
    type: "success",
  });

  // ✅ new state for delete modal
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedWord, setSelectedWord] = useState(null);

  const modalRef = useRef(null);

  // Fetch User Data
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));
        if (token && storedUser && storedUser.userId) {
          const userData = await getUserById(storedUser.userId, token);
          setUser({
            firstName: userData.firstName,
            lastName: userData.lastName,
            id: userData.id,
          });
        } else {
          throw new Error("User not found in localStorage");
        }
      } catch (error) {
        console.error("Failed to fetch user data:", error);
        handleLogout();
      }
    };
    fetchUserData();
  }, []);

  // Fetch Words Data
  useEffect(() => {
    const fetchWords = async () => {
      try {
        const data = await getWordsByLessonId(lessonId);
        setWords(data);
      } catch (error) {
        console.error("Error fetching words:", error);
        showNotification(
          "Error loading words. Please try refreshing the page.",
          "error"
        );
      } finally {
        setLoading(false);
      }
    };
    fetchWords();
  }, [lessonId]);

  /* ===============================
     LAYOUT & AUTH FUNCTIONS
  =============================== */
  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
    document.body.classList.toggle("sidebar-open", !sidebarOpen);
  };

  const handleSidebarNav = (section) => {
    navigate("/teacher-dashboard", { state: { defaultSection: section } });
  };

  const handleSidebarAdd = () => {
    setEditingWord(null);
    setNewWord({ word: "" });
    setImageFile(null);
    setEditImageFile(null);
    setShowModal(true);
  };

  /* ===============================
     NOTIFICATION FUNCTIONS
  =============================== */
  const showNotification = (message, type = "success") => {
    setNotification({ show: true, message, type });
    setTimeout(() => {
      setNotification((prev) => ({ ...prev, show: false }));
    }, 3000);
  };

  /* ===============================
     WORD CRUD FUNCTIONS
  =============================== */
  const handleAddWord = async (e) => {
    e.preventDefault();
    const userId = user.id;
    if (!userId) {
      showNotification("Error: User ID not found. Please log in again.", "error");
      return;
    }

    try {
      if (editingWord) {
        const updatedWord = {
          word: newWord.word,
          lesson: { lessonId },
          createdBy: { id: editingWord.createdBy.id },
        };
        const formData = new FormData();
        formData.append("word", JSON.stringify(updatedWord));
        if (editImageFile) formData.append("image", editImageFile);
        await updateWord(editingWord.wordId, formData);
        showNotification("Word updated successfully!");
      } else {
        const createdDate = new Date().toISOString();
        await createWord(newWord.word, imageFile, lessonId, userId, createdDate);
        showNotification("Word added successfully!");
      }

      const updatedWords = await getWordsByLessonId(lessonId);
      setWords(updatedWords);
      setShowModal(false);
      setNewWord({ word: "" });
      setImageFile(null);
      setEditingWord(null);
      setEditImageFile(null);
    } catch (error) {
      console.error("Error saving word:", error);
      showNotification("Failed to save word. Please try again.", "error");
    }
  };

  // ✅ Updated delete logic to show modal instead of window.confirm
  const handleDeleteWord = (word) => {
    setSelectedWord(word);
    setShowDeleteModal(true);
  };

  const confirmDeleteWord = async () => {
    if (!selectedWord) return;
    try {
      await deleteWord(selectedWord.wordId);
      const updatedWords = await getWordsByLessonId(lessonId);
      setWords(updatedWords);
      setShowDeleteModal(false);
      showNotification("Word deleted successfully!");
    } catch (error) {
      console.error("Error deleting word:", error);
      showNotification("Failed to delete word. Please try again.", "error");
    }
  };

  const handleEditWord = (word) => {
    setEditingWord(word);
    setNewWord({ word: word.word });
    setImageFile(null);
    setEditImageFile(null);
    setShowModal(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewWord((prev) => ({ ...prev, [name]: value }));
  };

  const playAudio = (audioURL) => {
    const audio = new Audio(`https://it332group46-pronounceit-production.up.railway.app${audioURL}`);
    audio.play();
  };

  /* ===============================
     MODAL & NOTIFICATION RENDERING
  =============================== */
  const closeModal = (e) => {
    if (modalRef.current && !modalRef.current.contains(e.target)) {
      setShowModal(false);
      setEditingWord(null);
    }
  };

  useEffect(() => {
    if (showModal) {
      document.addEventListener("mousedown", closeModal);
    } else {
      document.removeEventListener("mousedown", closeModal);
    }
    return () => document.removeEventListener("mousedown", closeModal);
  }, [showModal]);

  const renderNotification = () => {
    if (!notification.show) return null;
    return ReactDOM.createPortal(
      <div className="notification-overlay">
        <div className={`notification-modal ${notification.type}`}>
          <div className="notification-icon">
            {notification.type === "success" && "✓"}
            {notification.type === "error" && "✗"}
            {notification.type === "info" && "ℹ"}
          </div>
          <div className="notification-content">
            <p>{notification.message}</p>
            <button
              onClick={() =>
                setNotification((prev) => ({ ...prev, show: false }))
              }
              className="notification-button"
            >
              Close
            </button>
          </div>
        </div>
      </div>,
      document.body
    );
  };

  const renderModal = () => {
    if (!showModal) return null;
    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={modalRef}>
          <h3>{editingWord ? "Edit Word" : "Add New Word"}</h3>
          <form className="modal-form" onSubmit={handleAddWord}>
            <div className="form-group">
              <label htmlFor="word">Word</label>
              <input
                type="text"
                id="word"
                name="word"
                value={newWord.word}
                onChange={handleInputChange}
                placeholder="Enter the word"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="image">Upload Image</label>
              <input
                type="file"
                id="image"
                accept="image/*"
                onChange={(e) =>
                  editingWord
                    ? setEditImageFile(e.target.files[0])
                    : setImageFile(e.target.files[0])
                }
              />
            </div>
            <div className="modal-actions">
              <button
                type="button"
                className="cancel-btn"
                onClick={() => {
                  setShowModal(false);
                  setEditingWord(null);
                }}
              >
                Cancel
              </button>
              <button type="submit" className="submit-btn">
                {editingWord ? "Update Word" : "Add Word"}
              </button>
            </div>
          </form>
        </div>
      </div>,
      document.body
    );
  };

  /* ===============================
     MAIN JSX RENDER
  =============================== */
  return (
    <div className="dashboard-container">
      <Header
        isDashboard={true}
        user={user}
        onLogout={handleLogout}
        toggleSidebar={toggleSidebar}
        sidebarOpen={sidebarOpen}
        pageTitle={`Words for ${lessonName}`}
      />
      <SidebarLayout
        activeSection="lessons"
        handleNavClick={handleSidebarNav}
        sidebarOpen={sidebarOpen}
        onAddButtonClick={handleSidebarAdd}
        isWordsPage={true}
      >
        <div className="existing-items">
          <div className="table-wrapper">
            {loading ? (
              <p style={{ textAlign: "center", padding: "20px" }}>Loading...</p>
            ) : (
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Word</th>
                    <th>Image</th>
                    <th>Audio</th>
                    <th>Created By</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {words.length === 0 ? (
                    <tr>
                      <td colSpan="6" style={{ textAlign: "center" }}>
                        No words found for this lesson.
                      </td>
                    </tr>
                  ) : (
                    words.map((word) => (
                      <tr key={word.wordId}>
                        <td>{word.word}</td>
                        <td>
                          <img
                            src={`https://it332group46-pronounceit-production.up.railway.app${word.imageURL}`}
                            alt={word.word}
                            style={{
                              width: "100px",
                              height: "auto",
                              borderRadius: "5px",
                            }}
                          />
                        </td>
                        <td>
                          {word.audioURL ? (
                            <button
                              className="play-audio-btn"
                              onClick={() => playAudio(word.audioURL)}
                            >
                              Play Audio
                            </button>
                          ) : (
                            <span>No Audio</span>
                          )}
                        </td>
                        <td>{`${word.createdBy.firstName} ${word.createdBy.lastName}`}</td>
                        <td>{new Date(word.createdDate).toLocaleDateString()}</td>
                      {/* === THIS IS THE MODIFIED SECTION === */}
                        <td className="lesson-actions-cell">
                          <button
                            className="lesson-action-btn" // <-- Uses same class as other pages
                            onClick={() => handleEditWord(word)}
                          >
                            <FontAwesomeIcon icon={faEdit} /> {/* <-- Icon instead of text */}
                          </button>
                          <button
                            className="lesson-action-btn" // <-- Uses same class as other pages
                            style={{ // <-- Style matches delete button on other pages
                              backgroundColor: "rgba(229, 62, 62, 0.8)",
                              color: "white",
                            }}
                            onClick={() => handleDeleteWord(word)}
                          >
                            <FontAwesomeIcon icon={faTrash} /> {/* <-- Icon instead of text */}
                          </button>
                        </td>
                        {/* === END OF MODIFIED SECTION === */}
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </SidebarLayout>

      {/* Modals and Notifications */}
      {renderModal()}
      {renderNotification()}

      {/* ✅ Unified Delete Modal */}
      {showDeleteModal && (
        <div className="modal-overlay">
          <div className="modal-container">
            <h3 className="text-center text-lg font-semibold mb-3">
              Delete Word
            </h3>
            <p className="text-sm text-gray-600 mb-4">
              Are you sure you want to delete{" "}
              <strong>{selectedWord?.word}</strong>?
            </p>
            <div className="modal-actions">
              <button
                className="cancel-btn"
                onClick={() => setShowDeleteModal(false)}
              >
                Cancel
              </button>
              <button className="delete-btn" onClick={confirmDeleteWord}>
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ✅ Success/Error popup */}
      {notification.show && (
        <ModalSuccess
          show={notification.show}
          message={notification.message}
          type={notification.type}
          onClose={() =>
            setNotification({ show: false, message: "", type: "" })
          }
        />
      )}
    </div>
  );
};

export default Words;