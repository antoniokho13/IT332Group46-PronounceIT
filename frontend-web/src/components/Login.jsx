import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom'; // Added useNavigate for programmatic navigation
import '../assets/css/Login.css'; // Fixed import path
import logo from '../assets/images/logo.png';

const Login = () => {
  const navigate = useNavigate(); // Hook for programmatic navigation
  const [isLogin, setIsLogin] = useState(true);
  const [showRoleSelection, setShowRoleSelection] = useState(false);
  const [selectedRole, setSelectedRole] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    role: ''
  });
  const [errors, setErrors] = useState({});

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
    
    if (!formData.username) newErrors.username = 'Username is required';
    if (!formData.password) newErrors.password = 'Password is required';
    if (!isLogin) {
      if (!formData.email) newErrors.email = 'Email is required';
      if (!formData.role) newErrors.role = 'Please select a role';
      if (formData.password !== formData.confirmPassword) {
        newErrors.confirmPassword = 'Passwords do not match';
      }
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    // In a real app, you would call your API here
    if (isLogin) {
      alert(`Welcome back, ${formData.username}!`);
      // After successful login, navigate to dashboard
      navigate('/dashboard');
    } else {
      alert(`Account created for ${formData.username} (${formData.role})!\nYou can now log in.`);
      // Reset form after successful registration
      resetForm();
      setIsLogin(true); // Switch back to login view
    }
  };

  const handleRoleSelect = (role) => {
    setSelectedRole(role);
    setFormData(prev => ({ ...prev, role }));
    setShowRoleSelection(false);
  };

  const resetForm = () => {
    setFormData({
      username: '',
      password: '',
      confirmPassword: '',
      email: '',
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

  if (showRoleSelection) {
    return (
      <div className="login-container">
        <div className="login-box role-selection">
          <div className="login-header">
            <img 
              src={logo} 
              alt="PronounceIT" 
              className="app-logo" 
              onClick={() => navigate('/')} // Navigate to home page when logo is clicked
              style={{ cursor: 'pointer' }}
            />
            <h1>Join as...</h1>
            <p className="login-subtitle">Are you a Teacher or Student?</p>
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
    );
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <img 
            src={logo} 
            alt="PronounceIT" 
            className="app-logo" 
            onClick={() => navigate('/')} // Navigate to home page when logo is clicked
            style={{ cursor: 'pointer' }}
          />
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
              <div className="form-group role-display">
                <label>Account Type</label>
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
            )}

            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Enter your username"
                className={errors.username ? 'error' : ''}
                required
              />
              {errors.username && <span className="error-message">{errors.username}</span>}
            </div>

            {!isLogin && (
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="your@email.com"
                  className={errors.email ? 'error' : ''}
                  required
                />
                {errors.email && <span className="error-message">{errors.email}</span>}
              </div>
            )}

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

            <button type="submit" className="login-button">
              {isLogin ? 'Sign In' : 'Create Account'}
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
  );
};

export default Login;