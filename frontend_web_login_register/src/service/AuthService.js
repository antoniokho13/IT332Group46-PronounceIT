import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth'; // Replace with your backend URL if different

const AuthService = {
  /**
   * Sends a login request to the backend.
   * @param {Object} credentials - The login credentials (email and password).
   * @returns {Promise} - The response from the backend.
   */
  login: async (credentials) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, credentials);
      return response.data; // Contains token, userId, email, role, etc.
    } catch (error) {
      throw error.response ? error.response.data : new Error('Login failed');
    }
  },

  /**
   * Sends a register request to the backend.
   * @param {Object} userData - The user data for registration.
   * @returns {Promise} - The response from the backend.
   */
  register: async (userData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/register`, userData);
      return response.data; // Contains the created user data
    } catch (error) {
      throw error.response ? error.response.data : new Error('Registration failed');
    }
  },
};

export default AuthService;