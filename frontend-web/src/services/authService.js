import axios from "axios";

const API_BASE_URL = "https://it332group46-pronounceit-production.up.railway.app/api/auth"; // Replace with your backend URL

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

// --- UPDATED LOGOUT FUNCTION ---
export const logout = async () => {
  try {
    // 1. Tell the backend to blacklist the token (you are already doing this)
    await axios.post(`${API_BASE_URL}/logout`);
  } catch (error) {
    // Log the error but proceed with local logout anyway
    console.error("Backend logout failed, proceeding with local logout:", error.response?.data?.message || "Logout failed");
  } finally {
    // 2. CRITICAL: Clear the user's state from the browser's storage
    //    This is the fix for your frontend state.
    //    (Adjust "token" and "user" if you use different keys)
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    
    // 3. Optional: Force a redirect to the login page
    //    This is good practice.
    window.location.href = '/login'; 
  }
};
