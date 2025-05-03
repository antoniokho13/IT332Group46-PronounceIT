import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ component: Component, requiredRole = null }) => {
  // Get user from localStorage
  const userStr = localStorage.getItem('user');
  const token = localStorage.getItem('token');
  
  // Check if user is logged in
  if (!userStr || !token) {
    return <Navigate to="/login" replace />;
  }
  
  const user = JSON.parse(userStr);
  
  // If a specific role is required, check user's role
  if (requiredRole && user.role !== requiredRole) {
    // Redirect to the appropriate dashboard instead of showing "unauthorized"
    const dashboardPath = user.role === "ADMIN" ? '/teacher-dashboard' : '/user-dashboard';
    return <Navigate to={dashboardPath} replace />;
  }
  
  // If everything is okay, render the protected component
  return <Component />;
};

export default ProtectedRoute;