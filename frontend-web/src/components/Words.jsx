import { faEdit, faPlus, faTrash } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom"; // Import useNavigate
import "../assets/css/Words.css"; // Make sure to import the CSS
import { createWord, deleteWord, getWordsByLessonId, updateWord } from "../services/wordService";

const Words = () => {
  const { lessonId } = useParams(); // Get the lesson ID from the URL
  const location = useLocation(); // Get the lesson name from the navigation state
  const navigate = useNavigate(); // Initialize navigate for navigation
  const [lessonName, setLessonName] = useState(location.state?.lessonName || ""); // Default to empty if not provided
  const [words, setWords] = useState([]); // State to store words
  const [loading, setLoading] = useState(true); // State to handle loading
  const [showModal, setShowModal] = useState(false); // State to handle modal visibility
  const [newWord, setNewWord] = useState({ word: "" }); // State for new word
  const [imageFile, setImageFile] = useState(null); // State for uploaded image
  const [editingWord, setEditingWord] = useState(null); // State for the word being edited
  const [editImageFile, setEditImageFile] = useState(null); // State for the updated image
  const [notification, setNotification] = useState({
    show: false,
    message: '',
    type: 'success'
  });
  const modalRef = useRef(null);

  useEffect(() => {
    // Fetch words by lesson ID
    const fetchWords = async () => {
      try {
        const data = await getWordsByLessonId(lessonId);
        setWords(data);
      } catch (error) {
        console.error("Error fetching words:", error);
        showNotification("Error loading words. Please try refreshing the page.", "error");
      } finally {
        setLoading(false);
      }
    };

    fetchWords();
  }, [lessonId]);

  const showNotification = (message, type = 'success') => {
    setNotification({
      show: true,
      message,
      type
    });
    // Auto-hide notification after 3 seconds
    setTimeout(() => {
      setNotification(prev => ({ ...prev, show: false }));
    }, 3000);
  };

  const handleAddWord = async (e) => {
    e.preventDefault();

    const userId = localStorage.getItem("userId");
    const createdDate = new Date().toISOString();

    try {
      if (editingWord) {
        const updatedWord = {
          word: newWord.word,
          lesson: { lessonId },
          createdBy: { id: userId },
        };
        const formData = new FormData();
        formData.append("word", JSON.stringify(updatedWord));
        if (editImageFile) {
          formData.append("image", editImageFile);
        }

        await updateWord(editingWord.wordId, formData);
        showNotification("Word updated successfully!");
      } else {
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

  const handleDeleteWord = async (wordId) => {
    if (window.confirm("Are you sure you want to delete this word?")) {
      try {
        await deleteWord(wordId);
        showNotification("Word deleted successfully!");

        // Refresh the words list
        const updatedWords = await getWordsByLessonId(lessonId);
        setWords(updatedWords);
      } catch (error) {
        console.error("Error deleting word:", error);
        showNotification("Failed to delete word. Please try again.", "error");
      }
    }
  };

  const handleEditWord = (word) => {
    setEditingWord(word);
    setNewWord({ word: word.word });
    setShowModal(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewWord((prev) => ({ ...prev, [name]: value }));
  };

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

  const playAudio = (audioURL) => {
    const audio = new Audio(`http://localhost:8080${audioURL}`); // Add the base URL
    audio.play();
  };

  // Render notification
  const renderNotification = () => {
    if (!notification.show) return null;

    return (
      <div className="notification-overlay">
        <div className={`notification-modal ${notification.type}`}>
          <div className="notification-icon">
            {notification.type === 'success' && '✓'}
            {notification.type === 'error' && '✗'}
            {notification.type === 'info' && 'ℹ'}
          </div>
          <div className="notification-content">
            <p>{notification.message}</p>
            <button 
              onClick={() => setNotification(prev => ({ ...prev, show: false }))} 
              className="notification-button"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="dashboard-container">
      {/* Back Button */}
      <button className="back-button" onClick={() => navigate("/teacher-dashboard")}>
        Back to Dashboard
      </button>

      <h1 className="dashboard-title">Words for Lesson: {lessonName}</h1>

      {/* Add Word Button */}
      <div className="section-header">
        <button
          className="add-button"
          onClick={() => setShowModal(true)}
        >
          <FontAwesomeIcon icon={faPlus} /> Add New Word
        </button>
      </div>

      <div className="existing-items">
        <h3>Existing Words</h3>
        {loading ? (
          <p>Loading...</p>
        ) : words.length === 0 ? (
          <p>No words found for this lesson.</p>
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
              {words.map((word) => (
                <tr key={word.wordId}>
                  <td>{word.word}</td>
                  <td>
                    <img
                      src={`http://localhost:8080${word.imageURL}`}
                      alt={word.word}
                      style={{ width: "100px", height: "auto", borderRadius: "5px" }}
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
                  <td className="action-buttons-cell">
                    <button
                      className="action-btn blue-btn"
                      onClick={() => handleEditWord(word)}
                    >
                      <FontAwesomeIcon icon={faEdit} /> Edit
                    </button>
                    <button
                      className="action-btn blue-btn"
                      onClick={() => handleDeleteWord(word.wordId)}
                    >
                      <FontAwesomeIcon icon={faTrash} /> Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
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
        </div>
      )}

      {renderNotification()}
    </div>
  );
};

export default Words;