import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api/auth"; // Replace with your backend URL

export const login = async (email, password) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/login`, { email, password });
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Login failed";
  }
};

export const register = async (userData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/register`, userData);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Registration failed";
  }
};

export const logout = async () => {
  try {
    await axios.post(`${API_BASE_URL}/logout`); // Call the backend logout endpoint
  } catch (error) {
    throw error.response?.data?.message || "Logout failed";
  }
};