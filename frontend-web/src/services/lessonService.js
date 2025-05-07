import axios from "axios";

const API_URL = "http://localhost:8080/api/lessons";

// Function to get the authorization header with the Bearer token
const getAuthHeader = () => {
  const token = localStorage.getItem("token"); // Assuming the token is stored in localStorage
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  };
};

// Get all lessons
export const getAllLessons = async () => {
  try {
    const response = await axios.get(API_URL, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error("Error fetching lessons:", error);
    throw error;
  }
};

// Get a lesson by ID
export const getLessonById = async (lessonId) => {
  try {
    const response = await axios.get(`${API_URL}/${lessonId}`, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error(`Error fetching lesson with ID ${lessonId}:`, error);
    throw error;
  }
};

// Create a new lesson
export const createLesson = async (lesson, userId) => {
  try {
    const payload = {
      ...lesson,
      createdBy: { id: userId },
      createdDate: new Date().toISOString(), // Automatically set to today's date
      active: true, // Default to active
    };
    const response = await axios.post(API_URL, payload, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error("Error creating lesson:", error);
    throw error;
  }
};

// Update an existing lesson
export const updateLesson = async (lessonId, updatedLesson) => {
  try {
    const response = await axios.put(
      `${API_URL}/${lessonId}`,
      updatedLesson,
      getAuthHeader()
    );
    return response.data;
  } catch (error) {
    console.error(`Error updating lesson with ID ${lessonId}:`, error);
    throw error;
  }
};

// Delete a lesson
export const deleteLesson = async (lessonId) => {
  try {
    await axios.delete(`${API_URL}/${lessonId}`, getAuthHeader());
  } catch (error) {
    console.error(`Error deleting lesson with ID ${lessonId}:`, error);
    throw error;
  }
};