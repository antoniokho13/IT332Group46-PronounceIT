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
import { getAllCategories, createCategory, updateCategory, deleteCategory } from "../services/categoryService"; // Import the service functions
import { getUserById } from "../services/userService"; // Import the service to fetch user data
import { getAllLessons, updateLesson, createLesson, deleteLesson } from "../services/lessonService"; // Import the service to fetch lessons
// Create static version without actual backend connections
const TeacherDashboard = () => {
  const [user, setUser] = useState({ firstName: "", lastName: "", id: null }); // Include `id` in the user state
  const [showDropdown, setShowDropdown] = useState(false);
  const [activeSection, setActiveSection] = useState("dashboard");
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [editingItem, setEditingItem] = useState(null);
  const [categories, setCategories] = useState([]); // State to store categories
  const [loading, setLoading] = useState(true); // State to handle loading
  const [lessons, setLessons] = useState([]); // State to store lessons
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
  const handleDelete = async (category) => {
    if (window.confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      try {
        await deleteCategory(category.categoryId); // Call the deleteCategory service
        alert("Category deleted successfully!");
  
        // Refresh the categories list
        const updatedCategories = await getAllCategories();
        setCategories(updatedCategories);
      } catch (error) {
        console.error("Error deleting category:", error);
        alert("Failed to delete category. Please try again.");
      }
    }
  };

  const handleDeleteLesson = async (lesson) => {
    if (window.confirm(`Are you sure you want to delete the lesson "${lesson.name}"?`)) {
      try {
        await deleteLesson(lesson.lessonId); // Call the deleteLesson service
        alert("Lesson deleted successfully!");
  
        // Refresh the lessons list
        const updatedLessons = await getAllLessons();
        setLessons(updatedLessons);
      } catch (error) {
        console.error("Error deleting lesson:", error);
        alert("Failed to delete lesson. Please try again.");
      }
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

    if (modalType === "categories") {
      return (
        <>
          <h3>{isEditing ? "Edit Category" : "Add New Category"}</h3>
          <form className="modal-form" onSubmit={handleAddCategory}>
            <div className="form-group">
              <label htmlFor="categoryName">Category Name</label>
              <input
                type="text"
                id="categoryName"
                placeholder="e.g., Vowel Sounds, Consonants"
                defaultValue={isEditing ? editingItem.name : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="categoryDescription">Description</label>
              <textarea
                id="categoryDescription"
                placeholder="Describe this category of pronunciation"
                defaultValue={isEditing ? editingItem.description : ""}
                required
              ></textarea>
            </div>
            <div className="modal-actions">
              <button
                type="button"
                className="cancel-btn"
                onClick={() => setShowModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="submit-btn">
                {isEditing ? "Update" : "Add"}
              </button>
            </div>
          </form>
        </>
      );
    }

    if (modalType === "lessons") {
      return (
        <>
          <h3>{isEditing ? "Edit Lesson" : "Add New Lesson"}</h3>
          <form className="modal-form" onSubmit={handleAddLesson}>
            {!isEditing && ( // Only show the category dropdown when not editing
              <div className="form-group">
                <label htmlFor="lessonCategory">Category</label>
                <select
                  id="lessonCategory"
                  defaultValue={isEditing ? editingItem.category.categoryId : ""}
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map((category) => (
                    <option key={category.categoryId} value={category.categoryId}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
            )}
            <div className="form-group">
              <label htmlFor="lessonTitle">Lesson Name</label>
              <input
                type="text"
                id="lessonTitle"
                placeholder="Enter lesson name"
                defaultValue={isEditing ? editingItem.name : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lessonFocus">Focus</label>
              <input
                type="text"
                id="lessonFocus"
                placeholder="Enter lesson focus"
                defaultValue={isEditing ? editingItem.focus : ""}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="lessonSequence">Sequence</label>
              <input
                type="number"
                id="lessonSequence"
                placeholder="Enter sequence number"
                defaultValue={isEditing ? editingItem.sequence : ""}
                required
              />
            </div>
            <div className="modal-actions">
              <button
                type="button"
                className="cancel-btn"
                onClick={() => setShowModal(false)}
              >
                Cancel
              </button>
              <button type="submit" className="submit-btn">
                {isEditing ? "Update" : "Add"}
              </button>
            </div>
          </form>
        </>
      );
    }

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
                    <th>Lesson Name</th>
                    <th>Focus</th>
                    <th>Sequence</th>
                    <th>Category</th>
                    <th>Created By</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>{renderLessonsTable()}</tbody>
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
                    <th>Created By</th>
                    <th>Created Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {renderCategoriesTable()}
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

  useEffect(() => {
    // Fetch categories when the component mounts
    const fetchCategories = async () => {
      try {
        const data = await getAllCategories();
        setCategories(data);
      } catch (error) {
        console.error("Error fetching categories:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  useEffect(() => {
    // Fetch lessons when the component mounts
    const fetchLessons = async () => {
      try {
        const data = await getAllLessons();
        setLessons(data); // Store the fetched lessons in state
      } catch (error) {
        console.error("Error fetching lessons:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchLessons();
  }, []);

  const renderCategoriesTable = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan="5">Loading...</td>
        </tr>
      );
    }

    if (categories.length === 0) {
      return (
        <tr>
          <td colSpan="5">No categories found.</td>
        </tr>
      );
    }

    return categories.map((category) => (
      <tr key={category.categoryId}>
        <td>{category.name}</td>
        <td>{category.description}</td>
        <td>{`${category.createdBy.firstName} ${category.createdBy.lastName}`}</td>
        <td>{new Date(category.createdDate).toLocaleDateString()}</td>
        <td>
          <button
            className="edit-btn"
            onClick={(e) => {
              e.stopPropagation();
              openModal("categories", category); // Open modal for editing
            }}
          >
            <FontAwesomeIcon icon={faPlus} /> {/* Replace with an edit icon */}
          </button>
          <button
            className="delete-btn"
            onClick={(e) => {
              e.stopPropagation();
              handleDelete(category); // Call handleDelete when the trash icon is clicked
            }}
          >
            <FontAwesomeIcon icon={faTrash} />
          </button>
        </td>
      </tr>
    ));
  };

  const renderLessonsTable = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan="7">Loading...</td>
        </tr>
      );
    }

    if (lessons.length === 0) {
      return (
        <tr>
          <td colSpan="7">No lessons found.</td>
        </tr>
      );
    }

    return lessons.map((lesson) => (
      <tr key={lesson.lessonId}>
        <td>{lesson.name}</td>
        <td>{lesson.focus}</td>
        <td>{lesson.sequence}</td>
        <td>{lesson.category.name}</td>
        <td>{`${lesson.createdBy.firstName} ${lesson.createdBy.lastName}`}</td>
        <td>{new Date(lesson.createdDate).toLocaleDateString()}</td>
        <td>
          <button
            className="edit-btn"
            onClick={(e) => {
              e.stopPropagation();
              openModal("lessons", lesson); // Open modal for editing
            }}
          >
            <FontAwesomeIcon icon={faPlus} /> {/* Replace with an edit icon */}
          </button>
          <button
            className="delete-btn"
            onClick={(e) => {
              e.stopPropagation();
              handleDeleteLesson(lesson); // Call handleDeleteLesson when the trash icon is clicked
            }}
          >
            <FontAwesomeIcon icon={faTrash} />
          </button>
        </td>
      </tr>
    ));
  };

  const fetchUserData = async () => {
    try {
      const token = localStorage.getItem("token");
      const storedUser = JSON.parse(localStorage.getItem("user"));
      if (token && storedUser && storedUser.userId) {
        const userData = await getUserById(storedUser.userId, token);
        setUser({
          firstName: userData.firstName,
          lastName: userData.lastName,
          id: userData.id, // Store the user ID
        });
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

  useEffect(() => {
    fetchUserData(); // Fetch user data when the component mounts
  }, []);

  const handleAddCategory = async (e) => {
    e.preventDefault();

    // Get the input values
    const name = e.target.categoryName.value;
    const description = e.target.categoryDescription.value;

    // Prepare the category object
    const newCategory = {
      name,
      description,
      createdBy: { id: user.id }, // Use the user ID from the state
      createdDate: new Date().toISOString(), // Automatically set to today's date
      active: true, // Always set to true
    };

    try {
      if (editingItem) {
        // Call the updateCategory function if editing
        await updateCategory(editingItem.categoryId, newCategory);
        alert("Category updated successfully!");
      } else {
        // Call the createCategory function if adding a new category
        await createCategory(newCategory, user.id);
        alert("Category added successfully!");
      }

      // Refresh the categories list
      const updatedCategories = await getAllCategories();
      setCategories(updatedCategories);

      // Close the modal
      setShowModal(false);
    } catch (error) {
      console.error("Error saving category:", error);
      alert("Failed to save category. Please try again.");
    }
  };

  const handleAddLesson = async (e) => {
    e.preventDefault();

    // Determine if we are editing or adding a new lesson
    const isEditing = editingItem !== null;

    // Get the input values
    const categoryId = isEditing
      ? editingItem.category.categoryId // Use the existing category ID when editing
      : e.target.lessonCategory?.value; // Get the value from the dropdown when adding

    const name = e.target.lessonTitle.value;
    const focus = e.target.lessonFocus.value;
    const sequence = parseInt(e.target.lessonSequence.value, 10);

    // Prepare the lesson object
    const newLesson = {
      category: { categoryId: parseInt(categoryId, 10) }, // Use the selected or existing category ID
      name,
      focus,
      sequence,
      createdBy: { id: user.id }, // Use the logged-in user's ID
      createdDate: new Date().toISOString(), // Automatically set to today's date
      active: true, // Always set to true
    };

    try {
      if (isEditing) {
        // Call the updateLesson function if editing
        await updateLesson(editingItem.lessonId, newLesson);
        alert("Lesson updated successfully!");
      } else {
        // Call the createLesson function if adding a new lesson
        await createLesson(newLesson, user.id);
        alert("Lesson added successfully!");
      }

      // Refresh the lessons list
      const updatedLessons = await getAllLessons();
      setLessons(updatedLessons);

      // Close the modal
      setShowModal(false);
    } catch (error) {
      console.error("Error saving lesson:", error);
      alert("Failed to save lesson. Please try again.");
    }
  };

  localStorage.setItem("userId", user.id); // Replace `user.id` with the actual user ID from the login response

  const userId = localStorage.getItem("userId");
  console.log("Retrieved userId:", userId);

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