import React, { useState } from 'react';
import './Login.css';
import logo from '../assets/logo.png';
import AuthService from '../service/AuthService'; // Import AuthService

const Login = () => {
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
    if (!isLogin) {
      if (!formData.firstName) newErrors.firstName = 'First name is required';
      if (!formData.lastName) newErrors.lastName = 'Last name is required';
      if (!formData.email) newErrors.email = 'Email is required';
      if (!formData.role) newErrors.role = 'Please select a role';
      if (formData.password !== formData.confirmPassword) {
        newErrors.confirmPassword = 'Passwords do not match';
      }
    } else {
      if (!formData.email) newErrors.email = 'Email is required';
      if (!formData.password) newErrors.password = 'Password is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    console.log('Form data being submitted:', formData); // Debugging log

    try {
        if (isLogin) {
            const response = await AuthService.login({
                email: formData.email,
                password: formData.password,
            });
            alert(`Welcome back, ${response.email}!`);
        } else {
            const response = await AuthService.register({
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                password: formData.password,
                role: formData.role,
            });
            console.log('Registration response:', response); // Debugging log
            alert(`Account created for ${response.email} (${response.role})!`);
            resetForm();
            setIsLogin(true);
        }
    } catch (error) {
        console.error('Error during registration:', error.response || error.message);
        if (error.response) {
            console.error('Backend response:', error.response.data); // Log backend error
        }
        alert(error.response?.data?.message || error.message || 'An error occurred');
    }
};
const handleRoleSelect = (role) => {
    setSelectedRole(role);
    const mappedRole = role === 'teacher' ? 'ADMIN' : 'USER'; // Map roles
    setFormData(prev => ({ ...prev, role: mappedRole })); // Set mapped role
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

  if (showRoleSelection) {
    return (
      <div className="login-container">
        <div className="login-box role-selection">
          <div className="login-header">
            <img src={logo} alt="PronounceIT" className="app-logo" />
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
          <img src={logo} alt="PronounceIT" className="app-logo" />
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
                <div className="form-group">
                  <label htmlFor="firstName">First Name</label>
                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    placeholder="Enter your first name"
                    className={errors.firstName ? 'error' : ''}
                    required
                  />
                  {errors.firstName && <span className="error-message">{errors.firstName}</span>}
                </div>
                <div className="form-group">
                  <label htmlFor="lastName">Last Name</label>
                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    placeholder="Enter your last name"
                    className={errors.lastName ? 'error' : ''}
                    required
                  />
                  {errors.lastName && <span className="error-message">{errors.lastName}</span>}
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
        </div>
      </div>
    </div>
  );
};

export default Login;