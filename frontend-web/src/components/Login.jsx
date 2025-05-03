import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../assets/css/Login.css';
import { login, register } from '../services/authService';

const Login = () => {
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [showRoleSelection, setShowRoleSelection] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);
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
      if (!formData.role) newErrors.role = 'Please select a role';
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
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response));
        
        // Redirect based on user role
        const dashboardPath = response.role === "ADMIN" ? '/teacher-dashboard' : '/user-dashboard';
        
        // Fix welcome message using the firstName from response or fallback to role
        const userName = response.firstName || (response.role === "ADMIN" ? "Teacher" : "Student");
        
        setNotification({
          show: true,
          message: `Welcome back, ${userName}!`,
          type: 'success',
          redirect: dashboardPath
        });
      } else {
        const role = selectedRole === 'teacher' ? 'ADMIN' : 'USER';
        const userData = { ...formData, role };
        delete userData.confirmPassword;

        const response = await register(userData);
        setNotification({
          show: true,
          message: `Account created for ${response.firstName} ${response.lastName}!`,
          type: 'success'
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
    setSelectedRole(role);
    setFormData(prev => ({ ...prev, role }));
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

  if (showRoleSelection) {
    return (
      <>
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
            <nav>
              <ul>
                <li><a href="/#features">Features</a></li>
                <li><a href="/#how-it-works">How It Works</a></li>
                <li><a href="/#team">Developers</a></li>
                <li><a href="/#testimonials">Testimonials</a></li>
                <li><a href="/#faq">FAQ</a></li>
              </ul>
            </nav>
            <div className="cta-button">
              {/* Login/Signup buttons removed, but container kept for layout */}
            </div>
          </div>
        </header>

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
                  Sign In
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
      <header>
        <div className="container">
          <div className="logo">
            <img 
              src={require('../assets/images/logo.png')} 
              alt="Pronounceit Logo"
            />
          </div>
          <nav>
            <ul>
              <li><a href="/#features">Features</a></li>
              <li><a href="/#how-it-works">How It Works</a></li>
              <li><a href="/#team">Developers</a></li>
              <li><a href="/#testimonials">Testimonials</a></li>
              <li><a href="/#faq">FAQ</a></li>
            </ul>
          </nav>
          <div className="cta-button">
            {/* Login/Signup buttons removed, but container kept for layout */}
          </div>
        </div>
      </header>

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
              <button type="submit" className="login-button" disabled={loading}>
                {loading ? 'Processing...' : isLogin ? 'Sign In' : 'Create Account'}
              </button>
            </form>
          )}
          <div className="login-footer">
            <p>
              {isLogin ? "Don't have an account?" : "Already registered?"}
              <button 
                onClick={isLogin ? handleSignUpClick : () => setIsLogin(true)} 
                className="switch-button"
              >
                {isLogin ? 'Sign Up' : 'Sign In'}
              </button>
            </p>
            <div className="home-link">
              <Link to="/">Back to Home</Link>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Login;