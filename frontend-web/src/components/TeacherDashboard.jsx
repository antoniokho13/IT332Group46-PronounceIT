import {
  faBookOpen,
  faChartLine,
  faFolder,
  faHome,
  faLayerGroup,
  faPlus,
  faSignOutAlt,
  faTrash,
  faUser,
  faUsers
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useEffect, useRef, useState } from "react";
import ReactDOM from "react-dom";
import { Link, useNavigate } from "react-router-dom";
import "../assets/css/Dashboard.css";
import logo from "../assets/images/logo.png";

// Create static version without actual backend connections
const TeacherDashboard = () => {
  const [user, setUser] = useState({ firstName: "Teacher", lastName: "User" });
  const [showDropdown, setShowDropdown] = useState(false);
  const [activeSection, setActiveSection] = useState("dashboard");
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [editingItem, setEditingItem] = useState(null);
  const dropdownRef = useRef(null);
  const userCardRef = useRef(null);
  const modalRef = useRef(null);
  const navigate = useNavigate();

  const handleLogout = () => {
    // Static version: Just navigate to login page
    navigate("/login");
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

  // Close modal when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        modalRef.current &&
        !modalRef.current.contains(event.target) &&
        showModal
      ) {
        setShowModal(false);
        setEditingItem(null);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [modalRef, showModal]);

  const toggleDropdown = (e) => {
    e.stopPropagation();
    setShowDropdown(!showDropdown);
  };

  const handleNavClick = (section) => {
    setActiveSection(section);
  };

  // Function to open modal with specific type
  const openModal = (type, item = null) => {
    setModalType(type);
    setEditingItem(item);
    setShowModal(true);
  };

  // Function to handle row click for editing
  const handleRowClick = (item, type) => {
    openModal(type, item);
  };
  
  // Function to delete an item (just a placeholder for now)
  const handleDelete = (item, type) => {
    if (window.confirm(`Are you sure you want to delete this ${type}?`)) {
      console.log(`Deleting ${type}:`, item);
      // In a real app, you would call an API here
    }
  };

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

  // Render modal content based on type
  const renderModalContent = () => {
    const isEditing = editingItem !== null;
    
    switch (modalType) {
      case 'difficulty':
        return (
          <>
            <h3>{isEditing ? "Edit Difficulty Level" : "Add New Difficulty Level"}</h3>
            <form className="modal-form">
              <div className="form-group">
                <label htmlFor="difficultyName">Difficulty Name</label>
                <input 
                  type="text" 
                  id="difficultyName" 
                  placeholder="e.g., Beginner, Intermediate, Advanced" 
                  defaultValue={isEditing ? editingItem.name : ""}
                />
              </div>
              <div className="form-group">
                <label htmlFor="difficultyDescription">Description</label>
                <textarea 
                  id="difficultyDescription" 
                  placeholder="Describe what learners can expect at this difficulty level"
                  defaultValue={isEditing ? editingItem.description : ""}
                ></textarea>
              </div>
              <div className="form-group">
                <label htmlFor="difficultyOrder">Display Order</label>
                <input 
                  type="number" 
                  id="difficultyOrder" 
                  placeholder="e.g., 1, 2, 3" 
                  defaultValue={isEditing ? editingItem.order : ""}
                />
              </div>
              <div className="form-group">
                <label>Status</label>
                <div className="radio-group">
                  <label>
                    <input 
                      type="radio" 
                      name="status" 
                      value="active" 
                      defaultChecked={!isEditing || editingItem.status === "Active"} 
                    /> Active
                  </label>
                  <label>
                    <input 
                      type="radio" 
                      name="status" 
                      value="inactive" 
                      defaultChecked={isEditing && editingItem.status === "Inactive"} 
                    /> Inactive
                  </label>
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="submit-btn">{isEditing ? "Update" : "Add"}</button>
              </div>
            </form>
          </>
        );
      case 'lessons':
        return (
          <>
            <h3>{isEditing ? "Edit Lesson" : "Add New Lesson"}</h3>
            <form className="modal-form">
              <div className="form-group">
                <label htmlFor="lessonTitle">Lesson Title</label>
                <input 
                  type="text" 
                  id="lessonTitle" 
                  placeholder="Enter lesson title" 
                  defaultValue={isEditing ? editingItem.title : ""}
                />
              </div>
              <div className="form-group">
                <label htmlFor="lessonCategory">Category</label>
                <select 
                  id="lessonCategory"
                  defaultValue={isEditing ? editingItem.category : ""}
                >
                  <option value="">Select a category</option>
                  <option value="Vowel Sounds">Vowel Sounds</option>
                  <option value="Consonant Sounds">Consonant Sounds</option>
                  <option value="Word Stress">Word Stress</option>
                  <option value="Intonation">Intonation</option>
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="lessonDifficulty">Difficulty Level</label>
                <select 
                  id="lessonDifficulty"
                  defaultValue={isEditing ? editingItem.difficulty : ""}
                >
                  <option value="">Select difficulty level</option>
                  <option value="Beginner">Beginner</option>
                  <option value="Intermediate">Intermediate</option>
                  <option value="Advanced">Advanced</option>
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="lessonDescription">Lesson Description</label>
                <textarea 
                  id="lessonDescription" 
                  placeholder="Describe the lesson content"
                  defaultValue={isEditing ? editingItem.description : ""}
                ></textarea>
              </div>
              <div className="form-group">
                <label htmlFor="lessonContent">Lesson Content</label>
                <textarea 
                  id="lessonContent" 
                  className="content-editor" 
                  placeholder="Enter detailed lesson content here"
                  defaultValue={isEditing ? editingItem.content : ""}
                ></textarea>
              </div>
              <div className="form-group">
                <label>Upload Audio Examples</label>
                <div className="file-upload-container">
                  <label className="file-upload-label">
                    <span>Choose audio files</span>
                    <input 
                      type="file" 
                      accept="audio/*" 
                      multiple 
                      className="file-upload-input" 
                      onChange={(e) => {
                        // Optional: Show selected file names
                        const fileCount = e.target.files.length;
                        if (fileCount > 0) {
                          const fileContainer = e.target.parentNode.parentNode;
                          let fileInfo = fileContainer.querySelector('.file-selected');
                          if (!fileInfo) {
                            fileInfo = document.createElement('div');
                            fileInfo.className = 'file-selected';
                            fileContainer.appendChild(fileInfo);
                          }
                          fileInfo.textContent = `${fileCount} file${fileCount !== 1 ? 's' : ''} selected`;
                        }
                      }}
                    />
                  </label>
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="submit-btn">{isEditing ? "Update" : "Add"}</button>
              </div>
            </form>
          </>
        );
      case 'categories':
        return (
          <>
            <h3>{isEditing ? "Edit Category" : "Add New Category"}</h3>
            <form className="modal-form">
              <div className="form-group">
                <label htmlFor="categoryName">Category Name</label>
                <input 
                  type="text" 
                  id="categoryName" 
                  placeholder="e.g., Vowel Sounds, Consonants" 
                  defaultValue={isEditing ? editingItem.name : ""}
                />
              </div>
              <div className="form-group">
                <label htmlFor="categoryDescription">Description</label>
                <textarea 
                  id="categoryDescription" 
                  placeholder="Describe this category of pronunciation"
                  defaultValue={isEditing ? editingItem.description : ""}
                ></textarea>
              </div>
              <div className="form-group">
                <label htmlFor="categoryIcon">Icon (Optional)</label>
                <input type="file" id="categoryIcon" accept="image/*" />
              </div>
              <div className="form-group">
                <label htmlFor="categoryOrder">Display Order</label>
                <input 
                  type="number" 
                  id="categoryOrder" 
                  placeholder="e.g., 1, 2, 3" 
                  defaultValue={isEditing ? editingItem.order : ""}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="submit-btn">{isEditing ? "Update" : "Add"}</button>
              </div>
            </form>
          </>
        );
      case 'classes':
        return (
          <>
            <h3>{isEditing ? "Edit Class" : "Add New Class"}</h3>
            <form className="modal-form">
              <div className="form-group">
                <label htmlFor="className">Class Name</label>
                <input 
                  type="text" 
                  id="className" 
                  placeholder="e.g., Pronunciation Basics 101" 
                  defaultValue={isEditing ? editingItem.name : ""}
                />
              </div>
              <div className="form-group">
                <label htmlFor="classDescription">Description</label>
                <textarea 
                  id="classDescription" 
                  placeholder="Describe this class and its purpose"
                  defaultValue={isEditing ? editingItem.description : ""}
                ></textarea>
              </div>
              <div className="form-group">
                <label htmlFor="classDifficulty">Difficulty Level</label>
                <select 
                  id="classDifficulty"
                  defaultValue={isEditing ? editingItem.difficulty : ""}
                >
                  <option value="">Select difficulty level</option>
                  <option value="Beginner">Beginner</option>
                  <option value="Intermediate">Intermediate</option>
                  <option value="Advanced">Advanced</option>
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="classCapacity">Student Capacity</label>
                <input 
                  type="number" 
                  id="classCapacity" 
                  placeholder="e.g., 30" 
                  defaultValue={isEditing ? editingItem.capacity : ""}
                />
              </div>
              <div className="form-group">
                <label htmlFor="classSchedule">Schedule</label>
                <input 
                  type="text" 
                  id="classSchedule" 
                  placeholder="e.g., Mon, Wed 10:00 AM - 11:30 AM" 
                  defaultValue={isEditing ? editingItem.schedule : ""}
                />
              </div>
              <div className="form-group">
                <label>Status</label>
                <div className="radio-group">
                  <label>
                    <input 
                      type="radio" 
                      name="status" 
                      value="active" 
                      defaultChecked={!isEditing || editingItem.status === "Active"} 
                    /> Active
                  </label>
                  <label>
                    <input 
                      type="radio" 
                      name="status" 
                      value="inactive" 
                      defaultChecked={isEditing && editingItem.status === "Inactive"} 
                    /> Inactive
                  </label>
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="submit-btn">{isEditing ? "Update" : "Add"}</button>
              </div>
            </form>
          </>
        );
      default:
        return null;
    }
  };

  // Modal component
  const renderModal = () => {
    if (!showModal) return null;

    return ReactDOM.createPortal(
      <div className="modal-overlay">
        <div className="modal-container" ref={modalRef}>
          {renderModalContent()}
        </div>
      </div>,
      document.body
    );
  };

  // Content sections based on activeSection state
  const renderContent = () => {
    switch(activeSection) {
      case 'difficulty':
        return (
          <>
            <h2 className="dashboard-title">Difficulty Levels Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('difficulty')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Difficulty
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Difficulty Levels</h3>
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Order</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr onClick={() => handleRowClick({ 
                      name: "Beginner", 
                      description: "Basic pronunciation for absolute beginners", 
                      order: 1, 
                      status: "Active" 
                    }, 'difficulty')}>
                    <td>Beginner</td>
                    <td>Basic pronunciation for absolute beginners</td>
                    <td>1</td>
                    <td>Active</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Beginner" }, 'difficulty');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "Intermediate", 
                      description: "More advanced pronunciation patterns", 
                      order: 2, 
                      status: "Active"
                    }, 'difficulty')}>
                    <td>Intermediate</td>
                    <td>More advanced pronunciation patterns</td>
                    <td>2</td>
                    <td>Active</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Intermediate" }, 'difficulty');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "Advanced", 
                      description: "Complex pronunciation rules and exceptions", 
                      order: 3, 
                      status: "Active"
                    }, 'difficulty')}>
                    <td>Advanced</td>
                    <td>Complex pronunciation rules and exceptions</td>
                    <td>3</td>
                    <td>Active</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Advanced" }, 'difficulty');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </>
        );
      case 'lessons':
        return (
          <>
            <h2 className="dashboard-title">Lessons Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('lessons')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Lesson
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Lessons</h3>
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Difficulty</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr onClick={() => handleRowClick({
                      title: "Basic Vowel Sounds",
                      category: "Vowel Sounds",
                      difficulty: "Beginner",
                      created: "2023-04-15",
                      description: "Introduction to basic vowel sounds",
                      content: "This lesson covers the fundamental vowel sounds in English..."
                    }, 'lessons')}>
                    <td>Basic Vowel Sounds</td>
                    <td>Vowel Sounds</td>
                    <td>Beginner</td>
                    <td>2023-04-15</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ title: "Basic Vowel Sounds" }, 'lessons');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      title: "Consonant Blends",
                      category: "Consonant Sounds",
                      difficulty: "Intermediate",
                      created: "2023-04-10",
                      description: "Working with complex consonant combinations",
                      content: "This lesson explores consonant blends that often cause difficulty..."
                    }, 'lessons')}>
                    <td>Consonant Blends</td>
                    <td>Consonant Sounds</td>
                    <td>Intermediate</td>
                    <td>2023-04-10</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ title: "Consonant Blends" }, 'lessons');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      title: "Word Stress in Multisyllabic Words",
                      category: "Word Stress",
                      difficulty: "Advanced",
                      created: "2023-04-05",
                      description: "Advanced techniques for word stress",
                      content: "This lesson focuses on the patterns of stress in longer words..."
                    }, 'lessons')}>
                    <td>Word Stress in Multisyllabic Words</td>
                    <td>Word Stress</td>
                    <td>Advanced</td>
                    <td>2023-04-05</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ title: "Word Stress in Multisyllabic Words" }, 'lessons');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </>
        );
      case 'categories':
        return (
          <>
            <h2 className="dashboard-title">Categories Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('categories')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Category
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Categories</h3>
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Lessons Count</th>
                    <th>Order</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr onClick={() => handleRowClick({
                      name: "Vowel Sounds",
                      description: "All lessons related to vowel pronunciation",
                      lessonsCount: 8,
                      order: 1
                    }, 'categories')}>
                    <td>Vowel Sounds</td>
                    <td>All lessons related to vowel pronunciation</td>
                    <td>8</td>
                    <td>1</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Vowel Sounds" }, 'categories');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "Consonant Sounds",
                      description: "All lessons related to consonant pronunciation",
                      lessonsCount: 12,
                      order: 2
                    }, 'categories')}>
                    <td>Consonant Sounds</td>
                    <td>All lessons related to consonant pronunciation</td>
                    <td>12</td>
                    <td>2</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Consonant Sounds" }, 'categories');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "Word Stress",
                      description: "Lessons focused on syllable stress patterns",
                      lessonsCount: 5,
                      order: 3
                    }, 'categories')}>
                    <td>Word Stress</td>
                    <td>Lessons focused on syllable stress patterns</td>
                    <td>5</td>
                    <td>3</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Word Stress" }, 'categories');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "Intonation",
                      description: "Lessons about sentence rhythm and intonation",
                      lessonsCount: 3,
                      order: 4
                    }, 'categories')}>
                    <td>Intonation</td>
                    <td>Lessons about sentence rhythm and intonation</td>
                    <td>3</td>
                    <td>4</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Intonation" }, 'categories');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </>
        );
      case 'classes':
        return (
          <>
            <h2 className="dashboard-title">Classes Management</h2>
            <div className="section-header">
              <button className="add-button" onClick={() => openModal('classes')}>
                <FontAwesomeIcon icon={faPlus} /> Add New Class
              </button>
            </div>
            <div className="existing-items">
              <h3>Existing Classes</h3>
              <table className="items-table">
                <thead>
                  <tr>
                    <th>Class Name</th>
                    <th>Description</th>
                    <th>Students</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr onClick={() => handleRowClick({
                      name: "Pronunciation Basics 101",
                      description: "Introductory class for beginners",
                      students: 24,
                      created: "2023-03-15"
                    }, 'classes')}>
                    <td>Pronunciation Basics 101</td>
                    <td>Introductory class for beginners</td>
                    <td>24</td>
                    <td>2023-03-15</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Pronunciation Basics 101" }, 'classes');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "Advanced Phonetics",
                      description: "Advanced class focusing on complex phonetics",
                      students: 18,
                      created: "2023-04-02"
                    }, 'classes')}>
                    <td>Advanced Phonetics</td>
                    <td>Advanced class focusing on complex phonetics</td>
                    <td>18</td>
                    <td>2023-04-02</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "Advanced Phonetics" }, 'classes');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                  <tr onClick={() => handleRowClick({
                      name: "English for Business",
                      description: "Specialized pronunciation for business contexts",
                      students: 15,
                      created: "2023-05-10"
                    }, 'classes')}>
                    <td>English for Business</td>
                    <td>Specialized pronunciation for business contexts</td>
                    <td>15</td>
                    <td>2023-05-10</td>
                    <td>
                      <button 
                        className="delete-btn" 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete({ name: "English for Business" }, 'classes');
                        }}
                      >
                        <FontAwesomeIcon icon={faTrash} />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </>
        );
      case 'analytics':
        return (
          <>
            <h2 className="dashboard-title">Student Analytics</h2>
            <div className="analytics-overview">
              <div className="analytics-card">
                <h3>Student Engagement</h3>
                <p>Weekly active users: <strong>52</strong></p>
                <p>Monthly active users: <strong>148</strong></p>
                <p>Average session time: <strong>18 minutes</strong></p>
              </div>
              <div className="analytics-card">
                <h3>Completion Rates</h3>
                <p>Beginner lessons: <strong>78%</strong></p>
                <p>Intermediate lessons: <strong>52%</strong></p>
                <p>Advanced lessons: <strong>34%</strong></p>
              </div>
              <div className="analytics-card">
                <h3>Top Performing Content</h3>
                <ol>
                  <li>Basic Vowel Sounds (92% completion)</li>
                  <li>Consonant Pairs (87% completion)</li>
                  <li>Word Stress Basics (81% completion)</li>
                </ol>
              </div>
            </div>
            <div className="report-progress">
              <h3>Student Progress Overview</h3>
              <div className="graph-container animate-bars">
                <div className="chart-bar beginner">
                  <div className="chart-bar-percentage">65%</div>
                  <div className="chart-bar-label">Beginner</div>
                </div>
                <div className="chart-bar intermediate">
                  <div className="chart-bar-percentage">45%</div>
                  <div className="chart-bar-label">Intermediate</div>
                </div>
                <div className="chart-bar advanced">
                  <div className="chart-bar-percentage">20%</div>
                  <div className="chart-bar-label">Advanced</div>
                </div>
              </div>
            </div>
          </>
        );
      default: // dashboard
        return (
          <>
            <h2 className="dashboard-title">Teacher Dashboard</h2>

            <div className="metrics">
              <div className="metric lessons">
                <p>Created Lessons</p>
                <h4>15</h4>
              </div>
              <div className="metric stars">
                <p>Active Students</p>
                <h4>48</h4>
              </div>
              <div className="metric hours">
                <p>Avg. Completion Rate</p>
                <h4>82%</h4>
              </div>
            </div>

            <div className="marketplace">
              <div className="card analytics">
                <p>Content Management</p>
                <h4>Recent Activities</h4>
                <p>3 new lessons added this week</p>
                <button onClick={() => setActiveSection('lessons')}>ADD NEW LESSON</button>
              </div>
              <div className="card flow">
                <p>Student Engagement</p>
                <h4>High Activity</h4>
                <p>85% of students active in the past week</p>
                <button onClick={() => setActiveSection('analytics')}>VIEW DETAILS</button>
              </div>
              <div className="card upgrade">
                <p>Difficulty Levels</p>
                <h4>3 Difficulty Levels</h4>
                <p>Beginner, Intermediate, Advanced</p>
                <button onClick={() => setActiveSection('difficulty')}>MANAGE LEVELS</button>
              </div>
            </div>

            <div className="orders">
              <h3>Recent Content Updates</h3>
              <ul>
                <li>Added "Common Pronunciation Mistakes" lesson to Intermediate difficulty</li>
                <li>Created new "Vowel Sounds" category with 5 exercises</li>
                <li>Updated feedback for "Consonant Blends" practice session</li>
                <li>Added audio examples to "Word Stress Patterns" lesson</li>
              </ul>
            </div>

            <div className="report-progress">
              <h3>Student Progress Overview</h3>
              <div className="graph-container animate-bars">
                <div className="chart-bar beginner">
                  <div className="chart-bar-percentage">65%</div>
                  <div className="chart-bar-label">Beginner</div>
                </div>
                <div className="chart-bar intermediate">
                  <div className="chart-bar-percentage">45%</div>
                  <div className="chart-bar-label">Intermediate</div>
                </div>
                <div className="chart-bar advanced">
                  <div className="chart-bar-percentage">20%</div>
                  <div className="chart-bar-label">Advanced</div>
                </div>
              </div>
            </div>
          </>
        );
    }
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="container">
          <div className="logo">
            <Link to="/">
              <img src={logo} alt="Pronounceit Logo" />
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

      <div className="dashboard single">
        <aside className="sidebar">
          <nav>
            <ul>
              <li 
                className={activeSection === "dashboard" ? "active" : ""}
                onClick={() => handleNavClick("dashboard")}
              >
                <FontAwesomeIcon icon={faHome} className="sidebar-icon" />
                Dashboard
              </li>
              <li 
                className={activeSection === "difficulty" ? "active" : ""}
                onClick={() => handleNavClick("difficulty")}
              >
                <FontAwesomeIcon icon={faLayerGroup} className="sidebar-icon" />
                Add Difficulty
              </li>
              <li 
                className={activeSection === "lessons" ? "active" : ""}
                onClick={() => handleNavClick("lessons")}
              >
                <FontAwesomeIcon icon={faBookOpen} className="sidebar-icon" />
                Add Lessons
              </li>
              <li 
                className={activeSection === "categories" ? "active" : ""}
                onClick={() => handleNavClick("categories")}
              >
                <FontAwesomeIcon icon={faFolder} className="sidebar-icon" />
                Add Categories
              </li>
              <li 
                className={activeSection === "classes" ? "active" : ""}
                onClick={() => handleNavClick("classes")}
              >
                <FontAwesomeIcon icon={faUsers} className="sidebar-icon" />
                Add Classes
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

        <main className="content">
          {renderContent()}
        </main>
      </div>

      {renderDropdown()}
      {renderModal()}
    </div>
  );
};

export default TeacherDashboard;