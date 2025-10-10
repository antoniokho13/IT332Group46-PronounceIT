import axios from 'axios';

const API_BASE_URL = "https://it332group46-pronounceit-production.up.railway.app/api/achievements";

/**
 * Get auth token from localStorage for authorization headers
 * @returns {Object} Headers object with Authorization token if available
 */
const getAuthHeader = () => {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
};

/**
 * Create a new achievement with image support
 * @param {Object} achievementData - The achievement data to create
 * @returns {Promise<Object>} Created achievement object
 */
export const createAchievement = async (achievementData) => {
  try {
    // Use FormData for all requests to keep it consistent
    const formData = new FormData();
    
    // Append all the fields individually to match backend controller parameters
    formData.append('title', achievementData.name);
    formData.append('description', achievementData.description);
    formData.append('pointsRequired', achievementData.pointsRequired || 100);
    formData.append('isActive', achievementData.isActive);
    
    // Only append badge file if it exists
    if (achievementData.badgeFile) {
      formData.append('badgeFile', achievementData.badgeFile);
    }
    
    const response = await axios.post(API_BASE_URL, formData, {
      headers: {
        ...getAuthHeader(),
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // Map response back to frontend format
    return {
      id: response.data.id,
      name: response.data.title,
      description: response.data.description,
      pointsRequired: response.data.pointsRequired || 100,
      isActive: response.data.isActive,
      createdDate: response.data.createdAt || new Date().toISOString(),
      badgeUrl: response.data.badgeImagePath ? 
        `https://it332group46-pronounceit-production.up.railway.app${response.data.badgeImagePath}` : null
    };
  } catch (error) {
    console.error("Error creating achievement:", error);
    if (error.response) {
      console.error("Response status:", error.response.status);
      console.error("Response data:", error.response.data);
    }
    throw new Error("Failed to create achievement. Please try again.");
  }
};

/**
 * Update an existing achievement with image support
 * @param {number} id - Achievement ID to update
 * @param {Object} achievementData - The updated achievement data
 * @returns {Promise<Object>} Updated achievement object
 */
export const updateAchievement = async (id, achievementData) => {
  try {
    // Use FormData for all requests to keep it consistent
    const formData = new FormData();
    
    // Append all the fields individually to match backend controller parameters
    formData.append('title', achievementData.name);
    formData.append('description', achievementData.description);
    formData.append('pointsRequired', achievementData.pointsRequired || 100);
    formData.append('isActive', achievementData.isActive);
    
    // Only append badge file if it exists
    if (achievementData.badgeFile) {
      formData.append('badgeFile', achievementData.badgeFile);
    }
    
    const response = await axios.put(`${API_BASE_URL}/${id}`, formData, {
      headers: {
        ...getAuthHeader(),
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // Map response back to frontend format
    return {
      id: response.data.id,
      name: response.data.title,
      description: response.data.description,
      pointsRequired: response.data.pointsRequired || 100,
      isActive: response.data.isActive,
      createdDate: response.data.createdAt || new Date().toISOString(),
      badgeUrl: response.data.badgeImagePath ? 
        `https://it332group46-pronounceit-production.up.railway.app${response.data.badgeImagePath}` : null
    };
  } catch (error) {
    console.error(`Error updating achievement ${id}:`, error);
    if (error.response) {
      console.error("Response status:", error.response.status);
      console.error("Response data:", error.response.data);
    }
    throw new Error("Failed to update achievement. Please try again.");
  }
};

// Note: Icon mapping functions removed since we simplified the achievement system
// Achievements now only require title, description, pointsRequired, and badge image

/**
 * Get all achievements from the backend
 * @returns {Promise<Array>} Array of achievement objects
 */
export const getAllAchievements = async () => {
  try {
    const response = await axios.get(API_BASE_URL, {
      headers: getAuthHeader()
    });
    
    // Map backend data to match the format expected by the component
    return response.data.map(achievement => ({
      id: achievement.id,
      name: achievement.title,
      description: achievement.description,
      pointsRequired: achievement.pointsRequired || 100,
      createdDate: achievement.createdAt || new Date().toISOString(),
      isActive: achievement.isActive,
      // Update the URL construction to match the server's resource handler path
      badgeUrl: achievement.badgeImagePath ? 
        (() => {
          const url = `https://it332group46-pronounceit-production.up.railway.app${achievement.badgeImagePath}`;
          console.log('Generated badge URL:', url, 'from path:', achievement.badgeImagePath);
          return url;
        })() : null
    }));
  } catch (error) {
    console.error("Error fetching achievements:", error);
    throw new Error("Failed to fetch achievements. Please try again.");
  }
};

/**
 * Delete an achievement by ID
 * @param {number} id - Achievement ID to delete
 * @returns {Promise<void>}
 */
export const deleteAchievement = async (id) => {
  try {
    await axios.delete(`${API_BASE_URL}/${id}`, {
      headers: getAuthHeader()
    });
  } catch (error) {
    console.error(`Error deleting achievement ${id}:`, error);
    throw new Error("Failed to delete achievement. Please try again.");
  }
};

/**
 * Toggle achievement active status
 * @param {number} id - Achievement ID to toggle
 * @returns {Promise<Object>} Updated achievement object
 */
export const toggleAchievementStatus = async (id) => {
  try {
    await axios.patch(`${API_BASE_URL}/${id}/toggle-status`, {}, {
      headers: getAuthHeader()
    });
    
    // Get the updated achievement
    const response = await axios.get(`${API_BASE_URL}/${id}`, {
      headers: getAuthHeader()
    });
    
    return {
      id: response.data.id,
      name: response.data.title,
      description: response.data.description,
      pointsRequired: response.data.pointsRequired || 100,
      isActive: response.data.isActive,
      createdDate: response.data.createdAt || new Date().toISOString(),
      badgeUrl: response.data.badgeImagePath ? 
        `https://it332group46-pronounceit-production.up.railway.app${response.data.badgeImagePath}` : null
    };
  } catch (error) {
    console.error(`Error toggling achievement status ${id}:`, error);
    throw new Error("Failed to update achievement status. Please try again.");
  }
};