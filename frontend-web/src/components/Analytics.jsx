import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import "../assets/css/Analytics.css";
import Header from "../layout/Header";
import SidebarLayout from "../layout/Sidebar";
import { logout } from "../services/authService";
import { getWordStatisticsByLessonId } from "../services/pronounciationAttemptService";
import { getAllScoreRecords } from "../services/scoreService";
import { getUserById } from "../services/userService"; // ✅ Import userService

const Analytics = () => {
  const { lessonId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const [user, setUser] = useState({ firstName: "", lastName: "" });
  const [scoreData, setScoreData] = useState([]);
  const [wordStats, setWordStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Pagination states
  const [scorePage, setScorePage] = useState(1);
  const [wordPage, setWordPage] = useState(1);
  const itemsPerPage = 5;

  const lessonName = location.state?.lessonName || "Lesson";

  /* ===============================
     FETCH USER DETAILS
  =============================== */
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const token = localStorage.getItem("token");
        const storedUser = JSON.parse(localStorage.getItem("user"));

        if (token && storedUser && storedUser.userId) {
          const userData = await getUserById(storedUser.userId, token);
          setUser({
            firstName: userData.firstName,
            lastName: userData.lastName,
            id: userData.id,
          });
        }
      } catch (err) {
        console.error("Error fetching user:", err);
      }
    };

    fetchUser();
  }, []);

  /* ===============================
     LOGOUT HANDLER
  =============================== */
  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  /* ===============================
     FETCH ANALYTICS DATA
  =============================== */
  useEffect(() => {
    const fetchScoreData = async () => {
      try {
        const allScores = await getAllScoreRecords();
        const lessonScores = allScores.filter(
          (score) => score.lesson.lessonId === parseInt(lessonId)
        );

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

        const transformedData = Object.values(groupedScores).map((u) => {
          const bestScore = Math.max(...u.scores);
          const totalPerfectScore = 10;
          const passThreshold = totalPerfectScore * 0.5;
          return {
            name: `${u.firstName} ${u.lastName}`,
            email: u.email,
            attempts: u.attempts,
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
        const transformedData = wordStatsData.map((stat) => ({
          word: stat.word,
          avgAccuracy: parseFloat(stat.avgAccuracy || 0).toFixed(2),
          avgAttempts: Math.ceil(parseFloat(stat.avgAttempts || 0)),
          avgCorrectlyPronounced: parseFloat(
            stat.avgCorrectlyPronounced || 0
          ).toFixed(2),
        }));
        setWordStats(transformedData);
      } catch (error) {
        console.error("Error fetching word stats:", error);
      }
    };

    fetchScoreData();
    fetchWordStats();
  }, [lessonId]);

  /* ===============================
     SIDEBAR TOGGLE
  =============================== */
  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
    document.body.classList.toggle("sidebar-open", !sidebarOpen);
  };

  const handleNavClick = (section) => {
    navigate("/teacher-dashboard", { state: { defaultSection: section } });
  };

  /* ===============================
     PAGINATION LOGIC
  =============================== */
  const scoreStartIndex = (scorePage - 1) * itemsPerPage;
  const scoreEndIndex = scoreStartIndex + itemsPerPage;
  const currentScores = scoreData.slice(scoreStartIndex, scoreEndIndex);
  const totalScorePages = Math.ceil(scoreData.length / itemsPerPage);

  const wordStartIndex = (wordPage - 1) * itemsPerPage;
  const wordEndIndex = wordStartIndex + itemsPerPage;
  const currentWords = wordStats.slice(wordStartIndex, wordEndIndex);
  const totalWordPages = Math.ceil(wordStats.length / itemsPerPage);

  /* ===============================
     RENDER LAYOUT
  =============================== */
  return (
    <div className="dashboard-container">
      <Header
        isDashboard={true}
        pageTitle={`${lessonName} Analytics`}
        user={user}
        onLogout={handleLogout}
        toggleSidebar={toggleSidebar}
        sidebarOpen={sidebarOpen}
      />

      <SidebarLayout
        activeSection="analytics"
        handleNavClick={handleNavClick}
        sidebarOpen={sidebarOpen}
      >
        <div className="analytics-page">
          {loading ? (
            <p className="analytics-loading">Loading...</p>
          ) : (
            <>
              {/* === Student Scores Table === */}
              <div className="analytics-table-wrapper">
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
                    {currentScores.length === 0 ? (
                      <tr>
                        <td colSpan="5">No data available.</td>
                      </tr>
                    ) : (
                      currentScores.map((data, index) => (
                        <tr key={index}>
                          <td>{data.name}</td>
                          <td>{data.email}</td>
                          <td>{data.attempts}</td>
                          <td>{data.bestScore}</td>
                          <td
                            className={
                              data.status === "Pass"
                                ? "status-pass"
                                : "status-fail"
                            }
                          >
                            {data.status}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>

                {/* Pagination */}
                {scoreData.length > itemsPerPage && (
                  <div className="pagination-container">
                    <button
                      onClick={() => setScorePage(scorePage - 1)}
                      disabled={scorePage === 1}
                    >
                      Previous
                    </button>
                    <span>
                      Page {scorePage} of {totalScorePages}
                    </span>
                    <button
                      onClick={() => setScorePage(scorePage + 1)}
                      disabled={scorePage === totalScorePages}
                    >
                      Next
                    </button>
                  </div>
                )}
              </div>

              {/* === Word Statistics Table === */}
              <div className="analytics-table-wrapper">
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
                    {currentWords.length === 0 ? (
                      <tr>
                        <td colSpan="4">No data available.</td>
                      </tr>
                    ) : (
                      currentWords.map((word, index) => (
                        <tr key={index}>
                          <td>{word.word}</td>
                          <td>{word.avgAccuracy}</td>
                          <td>{word.avgAttempts}</td>
                          <td>
                            <div className="progress-bar-container">
                              <div
                                className="progress-bar"
                                style={{
                                  width: `${word.avgCorrectlyPronounced}%`,
                                }}
                              ></div>
                              <span className="progress-bar-label">
                                {word.avgCorrectlyPronounced}%
                              </span>
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>

                {wordStats.length > itemsPerPage && (
                  <div className="pagination-container">
                    <button
                      onClick={() => setWordPage(wordPage - 1)}
                      disabled={wordPage === 1}
                    >
                      Previous
                    </button>
                    <span>
                      Page {wordPage} of {totalWordPages}
                    </span>
                    <button
                      onClick={() => setWordPage(wordPage + 1)}
                      disabled={wordPage === totalWordPages}
                    >
                      Next
                    </button>
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </SidebarLayout>
    </div>
  );
};

export default Analytics;
