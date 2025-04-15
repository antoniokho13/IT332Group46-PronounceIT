import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api/users"; // Replace with your backend URL

// Fetch user by ID
export const getUserById = async (id) => {
  try {
    const token = localStorage.getItem("token"); // Get token from localStorage
    if (!token) throw new Error("No token found");

    const response = await axios.get(`${API_BASE_URL}/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`, // Include token in Authorization header
      },
    });
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Failed to fetch user data";
  }
};

// Update user information
export const updateUser = async (id, updatedData, token) => {
    try {
      const response = await fetch(`${API_BASE_URL}/${id}`, {
        method: "PUT", // Ensure this matches the backend's HTTP method
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`, // Include the token for authentication
        },
        body: JSON.stringify(updatedData),
      });
  
      if (!response.ok) {
        throw new Error(`Failed to update user: ${response.statusText}`);
      }
  
      return await response.json(); // Return the updated user data
    } catch (error) {
      console.error("Error in updateUser service:", error);
      throw error;
    }
};

// Delete user account
export const deleteUser = async (id, token) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: "DELETE", // HTTP DELETE method
      headers: {
        Authorization: `Bearer ${token}`, // Include the token for authentication
      },
    });

    if (!response.ok) {
      throw new Error(`Failed to delete user: ${response.statusText}`);
    }

    return { message: "User deleted successfully" }; // Return success message
  } catch (error) {
    console.error("Error in deleteUser service:", error);
    throw error;
  }
};