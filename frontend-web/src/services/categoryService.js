import axios from 'axios';

const API_URL = 'http://localhost:8080/api/categories';

// Function to get the authorization header with the Bearer token
const getAuthHeader = () => {
    const token = localStorage.getItem('token'); // Assuming the token is stored in localStorage
    return {
        headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };
};

// Get all categories
export const getAllCategories = async () => {
    try {
        const response = await axios.get(API_URL, getAuthHeader());
        return response.data;
    } catch (error) {
        console.error('Error fetching categories:', error);
        throw error;
    }
};

// Get a category by ID
export const getCategoryById = async (categoryId) => {
    try {
        const response = await axios.get(`${API_URL}/${categoryId}`, getAuthHeader());
        return response.data;
    } catch (error) {
        console.error(`Error fetching category with ID ${categoryId}:`, error);
        throw error;
    }
};

// Create a new category
export const createCategory = async (category, userId) => {
    try {
        const payload = {
            ...category,
            createdBy: { id: userId },
        };
        const response = await axios.post(API_URL, payload, getAuthHeader());
        return response.data;
    } catch (error) {
        console.error('Error creating category:', error);
        throw error;
    }
};

// Update an existing category
export const updateCategory = async (categoryId, updatedCategory) => {
    try {
        const response = await axios.put(`${API_URL}/${categoryId}`, updatedCategory, getAuthHeader());
        return response.data;
    } catch (error) {
        console.error(`Error updating category with ID ${categoryId}:`, error);
        throw error;
    }
};

// Delete a category
export const deleteCategory = async (categoryId) => {
    try {
        await axios.delete(`${API_URL}/${categoryId}`, getAuthHeader());
    } catch (error) {
        console.error(`Error deleting category with ID ${categoryId}:`, error);
        throw error;
    }
};