import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import './App.css';
import AchievementManagement from './components/AchievementManagement';
import Analytics from './components/Analytics';
import Home from './components/Home';
import Login from './components/Login';
import ProtectedRoute from './components/ProtectedRoute';
import TeacherDashboard from './components/TeacherDashboard';
import UserDashboard from './components/UserDashboard';
import UserInformation from './components/UserInformation';
import UserManagement from './components/UserManagement';
import Words from './components/Words';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route 
          path="/user-dashboard" 
          element={<ProtectedRoute component={UserDashboard} requiredRole="STUDENT" />} 
        />
        <Route 
          path="/teacher-dashboard" 
          element={<ProtectedRoute component={TeacherDashboard} requiredRole="TEACHER" />} 
        />
        <Route path="/profile" element={<ProtectedRoute component={UserInformation} />} />
        <Route 
          path="/words/:lessonId" 
          element={<ProtectedRoute component={Words} requiredRole="TEACHER" />} 
        />
        <Route path="/analytics/:lessonId" element={<Analytics />} />
        <Route 
          path="/user-management" 
          element={<ProtectedRoute component={UserManagement} requiredRole="ADMIN" />} 
        />
        <Route 
          path="/achievement-management" 
          element={<ProtectedRoute component={AchievementManagement} requiredRole="ADMIN" />} 
        />
      </Routes>
    </Router>
  );
}

export default App;