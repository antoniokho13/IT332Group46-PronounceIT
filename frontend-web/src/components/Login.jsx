import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../assets/css/Login.css';
// Icons are needed for role selection and potentially modal, keep them imported
import pronounceLogo from '../assets/images/pronounce_logo.png';
import studentIcon from '../assets/images/studenticon.png';
import teacherIcon from '../assets/images/teachericon.png';
import ModalSuccess from '../layout/ModalSuccess'; // Using the renamed component
import { login, register } from '../services/authService';

// ImagePanel component remains the same
const ImagePanel = () => (
  <div className="image-panel">
    <div className="image-panel-content">
      <img src={pronounceLogo} alt="PronounceIT Logo" className="logo-graphic" />
      <h2>Welcome to PronounceIT</h2>
      <p>Your journey to perfect pronunciation starts here. Log in or sign up to get started.</p>
    </div>
  </div>
);

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();
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
    role: null
  });
  const [redirectTarget, setRedirectTarget] = useState(null);

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    if (queryParams.get('signup') === 'true') {
      setIsLogin(false);
      setShowRoleSelection(true);
    } else {
        setIsLogin(true);
        setShowRoleSelection(false);
    }

    document.body.classList.add('login-page');

    return () => {
      document.body.style.overflow = 'auto';
      document.body.classList.remove('login-page');
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
    else if (!/\S+@\S+\.\S+/.test(formData.email)) newErrors.email = 'Email address is invalid';
    if (!formData.password) newErrors.password = 'Password is required';

    if (!isLogin) {
      if (!formData.firstName) newErrors.firstName = 'First Name is required';
      if (!formData.lastName) newErrors.lastName = 'Last Name is required';
      if (!formData.role || !['TEACHER', 'STUDENT'].includes(formData.role)) {
        newErrors.role = 'Please select either Teacher or Student role';
      }
      if (!formData.confirmPassword) newErrors.confirmPassword = 'Confirm Password is required';
      else if (formData.password !== formData.confirmPassword) {
        newErrors.confirmPassword = 'Passwords do not match';
      }
       if (formData.password && formData.password.length < 6) {
         newErrors.password = 'Password must be at least 6 characters';
       }
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setRedirectTarget(null); // Clear previous redirect target
    try {
      if (isLogin) {
        const response = await login(formData.email, formData.password);
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response));

        let dashboardPath;
        // Switch case for dashboard path remains the same...
        switch (response.role) {
          case 'ADMIN':
            dashboardPath = '/achievement-management';
            break;
          case 'TEACHER':
            dashboardPath = '/teacher-dashboard';
            break;
          case 'STUDENT':
            dashboardPath = '/user-dashboard';
            break;
          default:
            dashboardPath = '/user-dashboard';
        }

        const userRole = response.role;

        // --- UPDATED LOGIC for welcomeName ---
        let welcomeName;
        switch(userRole) {
          case 'ADMIN':
            welcomeName = 'Admin';
            break;
          case 'TEACHER':
            welcomeName = 'Teacher'; // Use "Teacher"
            break;
          case 'STUDENT':
            welcomeName = 'Student'; // Use "Student"
            break;
          default:
            welcomeName = 'User'; // Fallback
        }

        setNotification({
          show: true,
          message: `Welcome back, ${welcomeName}!`, // Use the new welcomeName variable
          type: 'success',
          role: userRole, // Pass role for icon
        });
        // --- END UPDATED LOGIC ---

        setRedirectTarget(dashboardPath); // Set the target path for after modal close

      } else { // Registration
        const role = selectedRole === 'teacher' ? 'TEACHER' : 'STUDENT';
        const userData = { ...formData, role };
        delete userData.confirmPassword;

        const response = await register(userData);
        setNotification({
          show: true,
          message: `Account created successfully for ${response.firstName}! Please log in.`,
          type: 'success',
          role: null, // No specific role icon needed for generic success
        });
        setRedirectTarget('/login'); // Set target to redirect to login after close
      }
    } catch (error) {
       const errorMessage = error.response?.data?.message || error.message || 'An error occurred. Please try again.';
      setNotification({
        show: true,
        message: errorMessage,
        type: 'error',
        role: null // No role icon for errors
      });
      setRedirectTarget(null); // No redirect on error
    } finally {
      setLoading(false);
    }
  };


  const handleRoleSelect = (role) => {
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
  };

  // Switch to Sign Up flow
  const handleSignUpClick = () => {
    resetForm();
    setIsLogin(false);
    setShowRoleSelection(true);
    navigate('/login?signup=true');
  };

  // Switch to Log In flow
  const handleLoginClick = () => {
    resetForm();
    setIsLogin(true);
    setShowRoleSelection(false);
    setSelectedRole(null);
    navigate('/login');
  };

  // Go back from signup form to role selection
  const handleBackToRoleSelection = () => {
      setShowRoleSelection(true);
      resetForm();
      navigate('/login?signup=true');
  };

  // New function to handle closing the modal and redirecting
  const handleModalClose = () => {
    // Hide the modal first
    setNotification(prev => ({ ...prev, show: false }));

    // Then check if we need to redirect
    if (redirectTarget) {
      // Special case for registration success: force reload on login page
      if (redirectTarget === '/login' && !isLogin && notification.type === 'success') {
          window.location.href = '/login';
      } else {
           navigate(redirectTarget);
      }
      setRedirectTarget(null); // Clear the target after navigation
    }
  };

  // --- renderPageWrapper ---
  const renderPageWrapper = (content) => (
    <>
      {/* Use the ModalSuccess component here */}
      <ModalSuccess
        show={notification.show}
        message={notification.message}
        type={notification.type}
        role={notification.role}
        onClose={handleModalClose} // Pass the new close handler
        redirectPath={redirectTarget} // Pass the target path for button text logic
      />

      {/* Main container remains the same */}
      <div className="login-container">
        <div className="split-container">
          <div className="form-panel">
            {content}
          </div>
          <ImagePanel />
        </div>
      </div>
    </>
  );

  // --- Role Selection Screen ---
  if (showRoleSelection && !isLogin) {
    const roleSelectionContent = (
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
            <div className="role-icon"><img src={teacherIcon} alt="Teacher Icon" /></div>
            <h3>Teacher</h3>
            <p>Create Lessons and Manage Words</p>
          </button>
          <button
            className="role-button student"
            onClick={() => handleRoleSelect('student')}
          >
            <div className="role-icon"><img src={studentIcon} alt="Student Icon" /></div>
            <h3>Student</h3>
            <p>Practice pronunciation and learn</p>
          </button>
        </div>
        <div className="login-footer">
          <p>Already have an account?
            <button onClick={handleLoginClick} className="switch-button">
              Log In
            </button>
          </p>
          <div className="home-link">
            <Link to="/">Back to Home</Link>
          </div>
        </div>
      </div>
    );
    return renderPageWrapper(roleSelectionContent);
  }

  // --- Login/Signup Form Screen ---
  const formContent = (
    <div className="login-box">
      <div className="login-header">
         <h1>{isLogin ? 'Welcome Back!' : `Sign Up as ${selectedRole ? selectedRole.charAt(0).toUpperCase() + selectedRole.slice(1) : ''}`}</h1>
        <p className="login-subtitle">
          {isLogin ? 'Ready to practice your pronunciation?' : 'Create your account to get started!'}
        </p>
      </div>
      <form onSubmit={handleSubmit} className="login-form">
        {!isLogin && selectedRole && (
          <>
            <div className="form-group role-display">
              <div className="role-chip">
                {selectedRole === 'teacher' ? 'Teacher Account' : 'Student Account'}
                <button
                  type="button"
                  className="change-role-button"
                  onClick={handleBackToRoleSelection}
                >
                  Change
                </button>
              </div>
            </div>
            <div className="name-row">
              <div className="form-group">
                <label htmlFor="firstName">First Name</label>
                <input type="text" id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} className={errors.firstName ? 'error' : ''} required />
                {errors.firstName && <p className="error-message">{errors.firstName}</p>}
              </div>
              <div className="form-group">
                <label htmlFor="lastName">Last Name</label>
                <input type="text" id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} className={errors.lastName ? 'error' : ''} required />
                {errors.lastName && <p className="error-message">{errors.lastName}</p>}
              </div>
            </div>
          </>
        )}
        <div className="form-group">
          <label htmlFor="email">Email</label>
          <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter your email" className={errors.email ? 'error' : ''} required />
          {errors.email && <span className="error-message">{errors.email}</span>}
        </div>
        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input type="password" id="password" name="password" value={formData.password} onChange={handleChange} placeholder="Enter your password" className={errors.password ? 'error' : ''} required />
          {errors.password && <span className="error-message">{errors.password}</span>}
        </div>
        {!isLogin && selectedRole && (
          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} placeholder="Re-enter your password" className={errors.confirmPassword ? 'error' : ''} required />
            {errors.confirmPassword && <span className="error-message">{errors.confirmPassword}</span>}
          </div>
        )}
        <button type="submit" className="login-button" disabled={loading}>
          {loading ? 'Processing...' : isLogin ? 'Log In' : 'Create Account'}
        </button>
      </form>
      <div className="login-footer">
        {isLogin ? (
          <p>
            Don't have an account?
            <button onClick={handleSignUpClick} className="switch-button"> Sign Up </button>
          </p>
        ) : (
            selectedRole && (
            <p>
              Already have an account?
              <button onClick={handleLoginClick} className="switch-button"> Log In </button>
            </p>
          )
        )}
        <div className="home-link">
          <Link to="/">Back to Home</Link>
        </div>
      </div>
    </div>
  );

  return renderPageWrapper(formContent);
};

export default Login;