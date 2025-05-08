import React from 'react';
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import './App.css';
import Home from './components/Home';
import Login from './components/Login';
import ProtectedRoute from './components/ProtectedRoute';
import TeacherDashboard from './components/TeacherDashboard';
import UserDashboard from './components/UserDashboard';
import UserInformation from './components/UserInformation';
import Words from './components/Words'; // Import the Words component

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route 
          path="/user-dashboard" 
          element={<ProtectedRoute component={UserDashboard} requiredRole="USER" />} 
        />
        <Route 
          path="/teacher-dashboard" 
          element={<ProtectedRoute component={TeacherDashboard} requiredRole="ADMIN" />} 
        />
        <Route path="/profile" element={<ProtectedRoute component={UserInformation} />} />
        <Route 
          path="/words/:lessonId" 
          element={<ProtectedRoute component={Words} requiredRole="ADMIN" />} 
        /> {/* Add the Words route */}
      </Routes>
    </Router>
  );
}

export default App;