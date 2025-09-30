import { faBars, faTimes } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../assets/css/Login.css';
import { login, register } from '../services/authService';

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLogin, setIsLogin] = useState(true);
  const [showRoleSelection, setShowRoleSelection] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);
  const [menuOpen, setMenuOpen] = useState(false); // State for mobile menu
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [notification, setNotification] = useState({
    show: false,
    message: '',
    type: 'success',
    redirect: null
  });

  // Toggle mobile menu
  const toggleMenu = () => {
    setMenuOpen(!menuOpen);
    document.body.style.overflow = !menuOpen ? 'hidden' : 'auto';
  };

  // Close mobile menu
  const closeMenu = () => {
    setMenuOpen(false);
    document.body.style.overflow = 'auto';
  };

  // Check URL parameters on component mount to determine if we should show signup
  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    if (queryParams.get('signup') === 'true') {
      setIsLogin(false);
      setShowRoleSelection(true);
    }
    
    // Cleanup function to ensure body scrolling is restored
    return () => {
      document.body.style.overflow = 'auto';
    };
  }, [location]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.email) newErrors.email = 'Email is required';
    if (!formData.password) newErrors.password = 'Password is required';
    if (!isLogin) {
      if (!formData.firstName) newErrors.firstName = 'First Name is required';
      if (!formData.lastName) newErrors.lastName = 'Last Name is required';
      if (!formData.role || !['TEACHER', 'STUDENT'].includes(formData.role)) {
        newErrors.role = 'Please select either Teacher or Student role';
      }
      if (formData.password !== formData.confirmPassword) {
        newErrors.confirmPassword = 'Passwords do not match';
      }
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    try {
      if (isLogin) {
        const response = await login(formData.email, formData.password);
        console.log('Login response:', response); // Debug log to verify role
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response));
        
        // Redirect based on user role
        let dashboardPath;
        switch (response.role) {
          case 'ADMIN':
            // TODO: This path has not been added yet. Change this to the actual path for the admin dashboard.
            dashboardPath = '/achievement-management';
            break;
          case 'TEACHER':
            dashboardPath = '/teacher-dashboard';
            break;
          case 'STUDENT':
            dashboardPath = '/user-dashboard';
            break;
          default:
            dashboardPath = '/user-dashboard'; // Fallback for unexpected roles
        }
        
        // Set welcome message based on role and firstName
        const userName = response.role === 'ADMIN' ? 'Admin' : (response.firstName || response.role);
        
        setNotification({
          show: true,
          message: `Welcome back, ${userName}!`,
          type: 'success',
          redirect: dashboardPath
        });
      } else {
        // Ensure only TEACHER or STUDENT roles are sent during registration
        const role = selectedRole === 'teacher' ? 'TEACHER' : 'STUDENT';
        const userData = { ...formData, role };
        delete userData.confirmPassword;

        const response = await register(userData);
        setNotification({
          show: true,
          message: `Account created for ${response.firstName} ${response.lastName}!`,
          type: 'success',
          redirect: '/login' // Redirect to login after successful registration
        });
        resetForm();
        setIsLogin(true);
      }
    } catch (error) {
      setNotification({
        show: true,
        message: error.toString(),
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleRoleSelect = (role) => {
    // Only allow 'teacher' or 'student' roles
    if (role !== 'teacher' && role !== 'student') return;
    setSelectedRole(role);
    setFormData(prev => ({ ...prev, role: role.toUpperCase() }));
    setShowRoleSelection(false);
  };

  const resetForm = () => {
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: ''
    });
    setErrors({});
    setSelectedRole(null);
  };

  const handleSignUpClick = () => {
    resetForm();
    setShowRoleSelection(true);
    setIsLogin(false);
  };

  const handleCloseNotification = () => {
    if (notification.redirect) {
      navigate(notification.redirect);
    }
    setNotification(prev => ({ ...prev, show: false }));
  };

  // Updated header with mobile menu for both role selection and login screens
  const renderHeader = () => (
    <header>
      <div className="container">
        <div className="logo">
          <Link to="/">
            <img 
              src={require('../assets/images/logo.png')} 
              alt="Pronounceit Logo"
            />
          </Link>
        </div>
        
        <nav className={menuOpen ? 'active' : ''}>
          <ul>
            <li><a href="/#features" onClick={closeMenu}>Features</a></li>
            <li><a href="/#how-it-works" onClick={closeMenu}>How It Works</a></li>
            <li><a href="/#team" onClick={closeMenu}>Developers</a></li>
            <li><a href="/#testimonials" onClick={closeMenu}>Testimonials</a></li>
            <li><a href="/#faq" onClick={closeMenu}>FAQ</a></li>
          </ul>
          {/* Add mobile buttons similar to Home.jsx for consistency */}
          <div className="mobile-buttons">
            <Link to="/login" className={`btn ${isLogin ? 'btn-primary' : 'btn-secondary'}`} onClick={closeMenu}>Log In</Link>
            <Link to="/login?signup=true" className={`btn ${!isLogin ? 'btn-primary' : 'btn-secondary'}`} onClick={closeMenu}>Sign Up</Link>
          </div>
        </nav>
        
        <div className="mobile-menu-button" onClick={toggleMenu}>
          <FontAwesomeIcon icon={menuOpen ? faTimes : faBars} />
        </div>
      </div>
    </header>
  );
  
  // Mobile menu overlay
  const renderMobileOverlay = () => (
    menuOpen && <div className="mobile-menu-overlay" onClick={closeMenu}></div>
  );

  if (showRoleSelection) {
    return (
      <>
        {renderHeader()}
        {renderMobileOverlay()}
        
        <div className="login-container">
          <div className="login-box role-selection">
            <div className="login-header">
              <h1>Join as...</h1>
              <p className="login-subtitle">Choose your account type to get started</p>
            </div>
            
            <div className="role-options">
              <button 
                className="role-button teacher"
                onClick={() => handleRoleSelect('teacher')}
              >
                <div className="role-icon">👩‍🏫</div>
                <h3>Teacher</h3>
                <p>Create lessons and manage classes</p>
              </button>
              
              <button 
                className="role-button student"
                onClick={() => handleRoleSelect('student')}
              >
                <div className="role-icon">🧑‍🎓</div>
                <h3>Student</h3>
                <p>Practice pronunciation and learn</p>
              </button>
            </div>
            
            <div className="login-footer">
              <p>Already have an account? 
                <button onClick={() => {
                  setShowRoleSelection(false);
                  setIsLogin(true);
                }} className="switch-button">
                  Log In
                </button>
              </p>
            </div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      {renderHeader()}
      {renderMobileOverlay()}

      {notification.show && (
        <div className="notification-overlay">
          <div className={`notification-modal ${notification.type}`}>
            <div className="notification-icon">
              {notification.type === 'success' && '✓'}
              {notification.type === 'error' && '✗'}
              {notification.type === 'info' && 'ℹ'}
            </div>
            <div className="notification-content">
              <p>{notification.message}</p>
              <button onClick={handleCloseNotification} className="notification-button">
                {notification.redirect ? 'Continue' : 'Close'}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="login-container">
        <div className="login-box">
          {/* Add back button for non-login screens when a role is selected */}
          {!isLogin && selectedRole && (
            <button 
              className="back-button" 
              onClick={() => setShowRoleSelection(true)}
              aria-label="Go back to role selection"
            >
              ←
            </button>
          )}
          
          <div className="login-header">
            <h1>{isLogin ? 'Welcome Back!' : `Sign Up as ${selectedRole}`}</h1>
            <p className="login-subtitle">
              {isLogin ? 'Ready to practice your pronunciation?' : 'Create your account to get started!'}
            </p>
          </div>
          {!isLogin && !selectedRole && (
            <div className="role-prompt">
              <p>Please select your role first</p>
              <button 
                onClick={() => setShowRoleSelection(true)}
                className="select-role-button"
              >
                Select Role
              </button>
            </div>
          )}
          {(isLogin || selectedRole) && (
            <form onSubmit={handleSubmit} className="login-form">
              {!isLogin && (
                <>
                  <div className="form-group role-display" style={{ marginBottom: '6px' }}>
                    <label style={{ fontSize: '12px' }}>Account Type</label>
                    <div className="role-chip">
                      {selectedRole === 'teacher' ? '👩‍🏫 Teacher' : '🧑‍🎓 Student'}
                      <button 
                        type="button" 
                        className="change-role-button"
                        onClick={() => setShowRoleSelection(true)}
                      >
                        Change
                      </button>
                    </div>
                  </div>
                  <div className="name-row">
                    <div className="form-group">
                      <label htmlFor="firstName">First Name</label>
                      <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleChange}
                      />
                      {errors.firstName && <p className="error-message">{errors.firstName}</p>}
                    </div>
                    <div className="form-group">
                      <label htmlFor="lastName">Last Name</label>
                      <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                      />
                      {errors.lastName && <p className="error-message">{errors.lastName}</p>}
                    </div>
                  </div>
                </>
              )}
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter your email"
                  className={errors.email ? 'error' : ''}
                  required
                />
                {errors.email && <span className="error-message">{errors.email}</span>}
              </div>
              <div className="form-group">
                <label htmlFor="password">Password</label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Enter your password"
                  className={errors.password ? 'error' : ''}
                  required
                />
                {errors.password && <span className="error-message">{errors.password}</span>}
              </div>
              {!isLogin && (
                <div className="form-group">
                  <label htmlFor="confirmPassword">Confirm Password</label>
                  <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Re-enter your password"
                    className={errors.confirmPassword ? 'error' : ''}
                    required
                  />
                  {errors.confirmPassword && <span className="error-message">{errors.confirmPassword}</span>}
                </div>
              )}
              <button type="submit" className="login-button" disabled={loading} style={{ 
                marginTop: '15px',
                marginBottom: '5px',
                width: '100%',
                padding: '10px'
              }}>
                {loading ? 'Processing...' : isLogin ? 'Log In' : 'Create Account'}
              </button>
            </form>
          )}
          {isLogin && (
            <div className="login-footer">
              <p>
                Don't have an account?
                <button 
                  onClick={handleSignUpClick} 
                  className="switch-button"
                >
                  Sign Up
                </button>
              </p>
              <div className="home-link">
                <Link to="/">Back to Home</Link>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default Login;