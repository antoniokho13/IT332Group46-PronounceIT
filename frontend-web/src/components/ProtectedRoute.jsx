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
    // Redirect to the appropriate dashboard based on role
    let dashboardPath;
    switch (user.role) {
      case 'ADMIN':
        // TODO: This path has not been added yet. Change this to the actual path for the admin dashboard if needed.
        dashboardPath = '/achievement-management';
        break;
      case 'TEACHER':
        dashboardPath = '/teacher-dashboard';
        break;
      case 'USER':
        dashboardPath = '/user-dashboard';
        break;
      default:
        dashboardPath = '/login'; // Fallback to login for unexpected roles
    }
    return <Navigate to={dashboardPath} replace />;
  }
  
  // If everything is okay, render the protected component
  return <Component />;
};

export default ProtectedRoute;