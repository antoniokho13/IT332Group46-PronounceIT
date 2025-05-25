import React, { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { getAllScoreRecords } from "../services/scoreService";
import { getWordStatisticsByLessonId } from "../services/pronounciationAttemptService"; // Use the correct service

const Analytics = () => {
  const { lessonId } = useParams(); // Get lessonId from URL
  const location = useLocation(); // Get lessonName from state
  const [scoreData, setScoreData] = useState([]);
  const [wordStats, setWordStats] = useState([]);
  const [loading, setLoading] = useState(true);

  const lessonName = location.state?.lessonName || "Lesson";

  useEffect(() => {
    const fetchScoreData = async () => {
      try {
        const allScores = await getAllScoreRecords();

        // Filter scores for the selected lesson
        const lessonScores = allScores.filter(score => score.lesson.lessonId === parseInt(lessonId));

        // Group scores by user
        const groupedScores = lessonScores.reduce((acc, score) => {
          const userId = score.user.id;
          if (!acc[userId]) {
            acc[userId] = {
              firstName: score.user.firstName,
              lastName: score.user.lastName,
              email: score.user.email,
              attempts: 0,
              scores: [],
            };
          }
          acc[userId].attempts += 1;
          acc[userId].scores.push(score.score);
          return acc;
        }, {});

        // Transform grouped data into an array
        const transformedData = Object.values(groupedScores).map(user => {
          const bestScore = Math.max(...user.scores);
          const totalPerfectScore = 10; // Assuming the perfect score is 10
          const passThreshold = totalPerfectScore * 0.6;
          return {
            name: `${user.firstName} ${user.lastName}`,
            email: user.email,
            attempts: user.attempts,
            bestScore: `${bestScore}/10`,
            status: bestScore >= passThreshold ? "Pass" : "Fail",
          };
        });

        setScoreData(transformedData);
      } catch (error) {
        console.error("Error fetching score data:", error);
      } finally {
        setLoading(false);
      }
    };

    const fetchWordStats = async () => {
      try {
        const wordStatsData = await getWordStatisticsByLessonId(lessonId);

        // Transform the data into the required format
        const transformedData = wordStatsData.map(stat => ({
          word: stat.word,
          avgAccuracy: parseFloat(stat.avgAccuracy || 0).toFixed(2),
          avgAttempts: Math.ceil(parseFloat(stat.avgAttempts || 0)), // Round up to the nearest whole number
          avgCorrectlyPronounced: parseFloat(stat.avgCorrectlyPronounced || 0).toFixed(2),
        }));

        setWordStats(transformedData);
      } catch (error) {
        console.error("Error fetching word statistics:", error);
      }
    };

    fetchScoreData();
    fetchWordStats();
  }, [lessonId]);

  return (
    <div className="analytics-container">
      <h2>{lessonName} - Student Analytics</h2>
      {loading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table className="analytics-table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Email</th>
                <th>Attempts</th>
                <th>Score</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {scoreData.length === 0 ? (
                <tr>
                  <td colSpan="5">No data available.</td>
                </tr>
              ) : (
                scoreData.map((data, index) => (
                  <tr key={index}>
                    <td>{data.name}</td>
                    <td>{data.email}</td>
                    <td>{data.attempts}</td>
                    <td>{data.bestScore}</td>
                    <td>{data.status}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          <h3>Word Statistics</h3>
          <table className="analytics-table">
            <thead>
              <tr>
                <th>Word</th>
                <th>Average Accuracy</th>
                <th>Average Attempts</th>
                <th>Average Correctly Pronounced (%)</th>
              </tr>
            </thead>
            <tbody>
              {wordStats.length === 0 ? (
                <tr>
                  <td colSpan="4">No data available.</td>
                </tr>
              ) : (
                wordStats.map((word, index) => (
                  <tr key={index}>
                    <td>{word.word}</td>
                    <td>{word.avgAccuracy}</td>
                    <td>{word.avgAttempts}</td>
                    <td>{word.avgCorrectlyPronounced}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
};

export default Analytics;