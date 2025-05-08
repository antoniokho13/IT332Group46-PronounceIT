import axios from 'axios';

const API_URL = 'http://localhost:8080/api/words'; // Replace with your backend URL

// Helper function to get the Authorization header
const getAuthHeader = () => {
    const token = localStorage.getItem('token'); // Assuming the token is stored in localStorage
    return { Authorization: `Bearer ${token}` };
};

// Get all words
export const getAllWords = async () => {
    try {
        const response = await axios.get(API_URL, { headers: getAuthHeader() });
        return response.data;
    } catch (error) {
        console.error('Error fetching all words:', error);
        throw error;
    }
};

// Get a word by ID
export const getWordById = async (wordId) => {
    try {
        const response = await axios.get(`${API_URL}/${wordId}`, { headers: getAuthHeader() });
        return response.data;
    } catch (error) {
        console.error(`Error fetching word with ID ${wordId}:`, error);
        throw error;
    }
};

// Get words by lesson ID
export const getWordsByLessonId = async (lessonId) => {
    try {
        const response = await axios.get(`${API_URL}/lesson/${lessonId}`, {
            headers: {
                ...getAuthHeader(),
            },
        });
        return response.data;
    } catch (error) {
        console.error(`Error fetching words for lesson ID ${lessonId}:`, error);
        throw error;
    }
};

// Create a new word
export const createWord = async (word, imageFile, lessonId, userId) => {
    try {
        const formData = new FormData();
        formData.append('word', JSON.stringify({
            word: word,
            lesson: { lessonId: lessonId },
            createdBy: { id: userId }
        }));
        formData.append('image', imageFile);

        const response = await axios.post(API_URL, formData, {
            headers: {
                ...getAuthHeader(),
                'Content-Type': 'multipart/form-data'
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error creating word:', error);
        throw error;
    }
};

// Update a word
export const updateWord = async (wordId, updatedWord) => {
    try {
        const response = await axios.put(`${API_URL}/${wordId}`, updatedWord, { headers: getAuthHeader() });
        return response.data;
    } catch (error) {
        console.error(`Error updating word with ID ${wordId}:`, error);
        throw error;
    }
};

// Delete a word
export const deleteWord = async (wordId) => {
    try {
        await axios.delete(`${API_URL}/${wordId}`, { headers: getAuthHeader() });
    } catch (error) {
        console.error(`Error deleting word with ID ${wordId}:`, error);
        throw error;
    }
};

// Get an audio file
export const getAudioFile = async (filename) => {
    try {
        const response = await axios.get(`${API_URL}/audio/${filename}`, {
            headers: getAuthHeader(),
            responseType: 'blob' // To handle binary data
        });
        return response.data;
    } catch (error) {
        console.error(`Error fetching audio file ${filename}:`, error);
        throw error;
    }
};

// Get an image file
export const getImageFile = async (filename) => {
    try {
        const response = await axios.get(`${API_URL}/images/${filename}`, {
            headers: getAuthHeader(),
            responseType: 'blob' // To handle binary data
        });
        return response.data;
    } catch (error) {
        console.error(`Error fetching image file ${filename}:`, error);
        throw error;
    }
};