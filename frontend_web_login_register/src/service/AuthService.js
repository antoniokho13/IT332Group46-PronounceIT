import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth'; // Backend base URL

const AuthService = {
  login: async (credentials) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/login`, credentials);
      return response.data; // Contains token, userId, email, role, etc.
    } catch (error) {
      throw error.response ? error.response.data : new Error('Login failed');
    }
  },

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