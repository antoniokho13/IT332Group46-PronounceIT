import { faBars, faTimes } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
// Link is already imported, so we are ready to use it
import { Link } from 'react-router-dom';
import '../assets/css/Header.css'; // Correct path to your new CSS file

// Accept menuOpen, toggleMenu, and closeMenu as props from Home.jsx
function Header({ menuOpen, toggleMenu, closeMenu }) {
  return (
    <>
      {/* Mobile menu overlay moved from Home.jsx */}
      <div
        className={`mobile-menu-overlay ${menuOpen ? 'active' : ''}`}
        onClick={closeMenu}
      ></div>

      {/* Header moved from Home.jsx */}
      <header>
        <div className="container">
          <div className="logo">
            {/* VVV THIS IS THE CHANGE VVV
              I've wrapped your logo image in a <Link> component
              that points to the homepage "/".
            */}
            <Link to="/">
              <img
                src={require('../assets/images/logo.png')}
                alt="Pronounceit Logo"
              />
            </Link>
            {/* ^^^ THIS IS THE CHANGE ^^^ */}
          </div>

          <nav className={menuOpen ? 'active' : ''}>
            <ul>
              <li>
                <a href="#features" onClick={closeMenu}>
                  Features
                </a>
              </li>
              <li>
                <a href="#how-it-works" onClick={closeMenu}>
                  How It Works
                </a>
              </li>
              <li>
                <a href="#team" onClick={closeMenu}>
                  Developers
                </a>
              </li>
              <li>
                <a href="#testimonials" onClick={closeMenu}>
                  Testimonials
                </a>
              </li>
              <li>
                <a href="#faq" onClick={closeMenu}>
                  FAQ
                </a>
              </li>
            </ul>
            <div className="mobile-buttons">
              <Link
                to="/login"
                className="btn btn-secondary"
                onClick={closeMenu}
              >
                Log In
              </Link>
              <Link
                to="/login?signup=true"
                className="btn btn-primary"
                onClick={closeMenu}
              >
                Sign Up
              </Link>
            </div>
          </nav>

          <div className="header-right">
            {/* Desktop buttons - only visible in desktop */}
            <div className="desktop-buttons">
              <Link
                to="/login"
                className="btn btn-secondary"
                style={{ marginRight: '15px' }}
              >
                Log In
              </Link>
              <Link to="/login?signup=true" className="btn btn-secondary">
                Sign Up
              </Link>
            </div>

            {/* Mobile menu button */}
            <div className="mobile-menu-button" onClick={toggleMenu}>
              <FontAwesomeIcon icon={menuOpen ? faTimes : faBars} />
            </div>
          </div>
        </div>
      </header>
    </>
  );
}

export default Header;