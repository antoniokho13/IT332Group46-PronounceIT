import axios from "axios";

const API_URL = "https://it332group46-pronounceit-production.up.railway.app/api/score-records";

const getAuthHeader = () => {
  const token = localStorage.getItem("token");
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  };
};

// Get all score records
export const getAllScoreRecords = async () => {
  try {
    const response = await axios.get(API_URL, getAuthHeader());
    return response.data;
  } catch (error) {
    console.error("Error fetching score records:", error);
    throw error;
  }
};