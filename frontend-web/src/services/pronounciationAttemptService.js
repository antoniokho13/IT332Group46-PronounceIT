import axios from "axios";

const API_URL = "http://localhost:8080/api/pronounciation-attempts";

const getAuthHeader = () => {
  const token = localStorage.getItem("token");
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  };
};

// Fetch all pronunciation attempts
export const getAllPronounciationAttempts = async () => {
  try {
    const response = await axios.get(API_URL, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error("Error fetching pronunciation attempts:", error);
    throw error;
  }
};

// Fetch pronunciation attempts by lesson ID
export const getPronounciationAttemptsByLessonId = async (lessonId) => {
  try {
    const response = await axios.get(`${API_URL}?lessonId=${lessonId}`, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error(`Error fetching pronunciation attempts for lesson ID ${lessonId}:`, error);
    throw error;
  }
};

// Fetch word statistics by lesson ID
export const getWordStatisticsByLessonId = async (lessonId) => {
  try {
    const response = await axios.get(`${API_URL}/statistics?lessonId=${lessonId}`, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error(`Error fetching word statistics for lesson ID ${lessonId}:`, error);
    throw error;
  }
};