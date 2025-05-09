import React, { useEffect, useState, useRef } from "react";
import { useLocation, useParams } from "react-router-dom";
import { getWordsByLessonId, createWord, deleteWord } from "../services/wordService";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTrash } from "@fortawesome/free-solid-svg-icons";

const Words = () => {
  const { lessonId } = useParams(); // Get the lesson ID from the URL
  const location = useLocation(); // Get the lesson name from the navigation state
  const [lessonName, setLessonName] = useState(location.state?.lessonName || ""); // Default to empty if not provided
  const [words, setWords] = useState([]); // State to store words
  const [loading, setLoading] = useState(true); // State to handle loading
  const [showModal, setShowModal] = useState(false); // State to handle modal visibility
  const [newWord, setNewWord] = useState({ word: "" }); // State for new word
  const [imageFile, setImageFile] = useState(null); // State for uploaded image
  const modalRef = useRef(null);

  useEffect(() => {
    // Fetch words by lesson ID
    const fetchWords = async () => {
      try {
        const data = await getWordsByLessonId(lessonId);
        setWords(data);
      } catch (error) {
        console.error("Error fetching words:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchWords();
  }, [lessonId]);

  const handleAddWord = async (e) => {
    e.preventDefault();

    const userId = localStorage.getItem("userId"); // Fetch user ID from localStorage
    const createdDate = new Date().toISOString(); // Automatically set created date

    try {
      await createWord(newWord.word, imageFile, lessonId, userId, createdDate);
      alert("Word added successfully!");

      // Refresh the words list
      const updatedWords = await getWordsByLessonId(lessonId);
      setWords(updatedWords);

      // Close the modal
      setShowModal(false);
      setNewWord({ word: "" });
      setImageFile(null);
    } catch (error) {
      console.error("Error adding word:", error);
      alert("Failed to add word. Please try again.");
    }
  };

  const handleDeleteWord = async (wordId) => {
    if (window.confirm("Are you sure you want to delete this word?")) {
      try {
        await deleteWord(wordId);
        alert("Word deleted successfully!");

        // Refresh the words list
        const updatedWords = await getWordsByLessonId(lessonId);
        setWords(updatedWords);
      } catch (error) {
        console.error("Error deleting word:", error);
        alert("Failed to delete word. Please try again.");
      }
    }
  };

  const handleImageChange = (e) => {
    setImageFile(e.target.files[0]);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewWord((prev) => ({ ...prev, [name]: value }));
  };

  const closeModal = (e) => {
    if (modalRef.current && !modalRef.current.contains(e.target)) {
      setShowModal(false);
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

  return (
    <div className="dashboard-container">
      <h1 className="dashboard-title">Words for Lesson: {lessonName}</h1>
      <button className="add-button" onClick={() => setShowModal(true)}>
        Add Word
      </button>
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
              <th>Audio</th> {/* Moved Audio column after Image */}
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
                    src={`http://localhost:8080${word.imageURL}`} // Add the base URL
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
                <td>
                  <button
                    className="delete-btn"
                    onClick={() => handleDeleteWord(word.wordId)}
                  >
                    <FontAwesomeIcon icon={faTrash} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showModal && (
        <div className="modal-overlay">
          <div className="modal-container" ref={modalRef}>
            <h3>Add New Word</h3>
            <form onSubmit={handleAddWord}>
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
                  onChange={handleImageChange}
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="submit-btn">
                  Add Word
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Words;