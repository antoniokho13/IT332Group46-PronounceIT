
import { faFacebookF, faInstagram, faTwitter, faYoutube } from '@fortawesome/free-brands-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../assets/css/Home.css';

function Home() {
  useEffect(() => {
    // Function to handle smooth scrolling
    const handleSmoothScroll = (e) => {
      const href = e.currentTarget.getAttribute('href');
      
      // Only handle internal links
      if (href && href.startsWith('#')) {
        e.preventDefault();
        const targetId = href;
        const targetElement = document.querySelector(targetId);
        
        if (targetElement) {
          const headerOffset = 80; // Adjust based on your header height
          const elementPosition = targetElement.getBoundingClientRect().top;
          const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
          
          window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth'
          });
        }
      }
    };
  
    // Add event listeners for smooth scrolling
    const addSmoothScrollListeners = () => {
      const anchors = document.querySelectorAll('a[href^="#"]');
      anchors.forEach(anchor => {
        // Remove any existing listeners first
        anchor.removeEventListener('click', handleSmoothScroll);
        // Add the new listener
        anchor.addEventListener('click', handleSmoothScroll);
      });
    };
    
    // Set up intersection observer for scroll animations
    const setupScrollAnimations = () => {
      const sections = document.querySelectorAll('section');
      const animatedElements = document.querySelectorAll(
        '.feature-card, .team-member, .testimonial-card, .faq-item, .step, h2'
      );
      
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('animate-in');
            
            // For elements that should bounce
            if (entry.target.classList.contains('feature-card') || 
                entry.target.classList.contains('team-member')) {
              entry.target.classList.add('bounce-in');
            }
            
            // Only animate once
            observer.unobserve(entry.target);
          }
        });
      }, {
        threshold: 0.15,
        rootMargin: '-50px'
      });
      
      // Observe sections
      sections.forEach(section => {
        observer.observe(section);
      });
      
      // Observe individual elements
      animatedElements.forEach(element => {
        observer.observe(element);
      });
    };
  
    // Initial setup
    addSmoothScrollListeners();
    // Add a small delay to ensure DOM is fully loaded
    setTimeout(() => {
      setupScrollAnimations();
    }, 100);
    
    // Clean up function
    return () => {
      document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.removeEventListener('click', handleSmoothScroll);
      });
    };
  }, []);

  return (
    <>
      {/* Header */}
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
              <li><a href="#features">Features</a></li>
              <li><a href="#how-it-works">How It Works</a></li>
              <li><a href="#team">Developers</a></li>
              <li><a href="#testimonials">Testimonials</a></li>
              <li><a href="#faq">FAQ</a></li>
            </ul>
          </nav>
          <div className="cta-button">
             <Link to="/login" className="btn btn-secondary" style={{ marginRight: '15px' }}>Log In</Link>
             <Link to="/login?mode=signup" className="btn btn-secondary">Sign Up</Link>
          </div>
        </div>
      </header>
      {/* Hero Section */}
<section id="hero" className="gradient-background">
  <div className="container">
    <div className="hero-content">
      <h1>"Fun, easy, and clear PronounceIT helps every kid speak without fear!"</h1>
      <p>An interactive way for kids to learn through games and rewards!</p>
      <Link to="/signup" className="btn btn-primary">Get Started for Free</Link>
    </div>
    <div className="hero-image">
      <img 
        src={require('../assets/images/hero1.png')} 
        alt="Kids learning with PronounceIT" 
      />
    </div>
  </div>
</section> 

      {/* Features Section */}
<section id="features">
  <div className="container">
    <h2>Why Kids & Parents Love PronounceIT</h2>
    <div className="features-grid">
      <div className="feature-card">
        <div className="feature-icon gaming">🎮</div>
        <h3>Gamified Learning</h3>
        <p>Fun challenges and rewards keep kids engaged and excited to learn more.</p>
      </div>
      <div className="feature-card">
        <div className="feature-icon trophy">🏆</div>
        <h3>Achievements & Rewards</h3>
        <p>Earn badges, points, and prizes as you make progress on your learning journey.</p>
      </div>
      <div className="feature-card">
        <div className="feature-icon school">🏫</div>
        <h3>For Schools & Parents</h3>
        <p>Track progress, set goals, and customize learning paths for your children.</p>
      </div>
      <div className="feature-card">
        <div className="feature-icon globe">🌍</div>
        <h3>Safe & Kid-Friendly</h3>
        <p>No ads, no distractions, just a secure environment designed for children.</p>
      </div>
    </div>
  </div>
</section>

      {/* How It Works Section */}
      <section id="how-it-works">
        <div className="container">
          <h2>How PronounceIT Works</h2>
          <div className="steps">
            <div className="step">
              <div className="step-number">1</div>
              <h3>Sign Up</h3>
              <p>Create a free account in just a few clicks to get started.</p>
            </div>
            <div className="step">
              <div className="step-number">2</div>
              <h3>Choose Activities</h3>
              <p>Select from hundreds of fun learning games and activities.</p>
            </div>
            <div className="step">
              <div className="step-number">3</div>
              <h3>Earn Rewards</h3>
              <p>Complete challenges to earn points, badges, and unlock new content.</p>
            </div>
            <div className="step">
              <div className="step-number">4</div>
              <h3>Track Progress</h3>
              <p>Parents and teachers can monitor learning achievements.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Demo Section */}
      <section id="demo">
        <div className="container">
          <h2>See PronounceIT in Action</h2>
          <div className="demo-container">
            <img src="https://placehold.co/800x450/845EC2/FFF?text=Demo+Video+Placeholder" alt="EduPlay Demo" className="demo-image" />
            <div className="play-button">⏯️</div>
          </div>
        </div>
      </section>

      {/* Development Team Section */}
<section id="team">
  <div className="container">
    <h2>Meet Our Development Team</h2>
    <div className="team-grid">
      <div className="team-member">
        <div className="team-photo">
          {/* Placeholder for photo */}
          <img src={require('../assets/images/mel.png')} alt="Rommel John Pobadora" />
        </div>
        <h3>Rommel John Pobadora</h3>
        <p className="team-role">Frontend Developer</p>
      </div>
      
      <div className="team-member">
        <div className="team-photo">
          {/* Placeholder for photo */}
          <img src={require('../assets/images/raymund.png')} alt="Raymund Christian Laude" />
        </div>
        <h3>Raymund Christian Laude</h3>
        <p className="team-role">Mobile Developer</p>
      </div>
      
      <div className="team-member">
        <div className="team-photo">
          {/* Placeholder for photo */}
          <img src={require('../assets/images/antonio.png')} alt="Antonio Abangan Kho" />
        </div>
        <h3>Antonio Abangan Kho</h3>
        <p className="team-role">Lead Developer</p>
      </div>
      
      <div className="team-member">
        <div className="team-photo">
          {/* Placeholder for photo */}
          <img src={require('../assets/images/barry.png')} alt="Christian Barry Alico" />
        </div>
        <h3>Christian Barry Alico</h3>
        <p className="team-role">Backend Developer</p>
      </div>
      
      <div className="team-member">
        <div className="team-photo">
          {/* Placeholder for photo */}
          <img src={require('../assets/images/rhandulf.png')} alt="Rhandulf Saceda" />
        </div>
        <h3>Rhandulf Saceda</h3>
        <p className="team-role">UI/UX Designer</p>
      </div>
    </div>
  </div>
</section>

      {/* Testimonials Section */}
      <section id="testimonials">
        <div className="container">
          <h2>What Families Are Saying</h2>
          <div className="testimonials-grid">
            <div className="testimonial-card">
              <div className="testimonial-image">
                <img src="https://placehold.co/100x100/FF9671/FFF?text=Parent" alt="Parent" />
              </div>
              <div className="testimonial-content">
                <p>"My child loves learning with PronounceIT! It's amazing to see how excited he gets about speaking now."</p>
                <p className="testimonial-author">- Sarah J., Parent</p>
              </div>
            </div>
            <div className="testimonial-card">
              <div className="testimonial-image">
                <img src="https://placehold.co/100x100/845EC2/FFF?text=Teacher" alt="Teacher" />
              </div>
              <div className="testimonial-content">
                <p>"As a teacher, I appreciate how PronounceIT makes it easy to track student progress and identify areas where they need help."</p>
                <p className="testimonial-author">- Michael T.,  Elementary Teacher</p>
              </div>
            </div>
            <div className="testimonial-card">
              <div className="testimonial-image">
                <img src="https://placehold.co/100x100/00C9A7/FFF?text=Parent" alt="Parent" />
              </div>
              <div className="testimonial-content">
                <p>"The rewards system keeps my daughter motivated! She's always eager to earn new badges."</p>
                <p className="testimonial-author">- Lisa M., Parent</p>
              </div>
            </div>
          </div>
        </div>
      </section>


      {/* FAQ Section */}
      <section id="faq">
        <div className="container">
          <h2>Frequently Asked Questions</h2>
          <div className="faq-grid">
            <div className="faq-item">
              <h3>How does PronounceIT work?</h3>
              <p>PronounceIT uses gamification techniques to make learning fun for kids. Children complete interactive activities to earn points and rewards while building knowledge in various subjects.</p>
            </div>
            <div className="faq-item">
              <h3>Is PronounceIT safe for kids?</h3>
              <p>Absolutely! PronounceIT is designed with child safety as a priority. There are no ads, no external links, and all content is age-appropriate. Parents have full control over their child's account.</p>
            </div>
            <div className="faq-item">
              <h3>What subjects are covered?</h3>
              <p>PronounceIT content tailored to different age groups from preschool to middle school.</p>
            </div>
            <div className="faq-item">
              <h3>Can I cancel my subscription at any time?</h3>
              <p>Yes, you can cancel your subscription at any time. There are no long-term commitments.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Final CTA Section */}
      <section id="final-cta">
        <div className="container">
          <h2>Start Your Child's Fun Learning Journey Today!</h2>
          <p>Join thousands of families who are making education exciting with PronounceIT.</p>
          <Link to="/signup" className="btn btn-primary btn-large">Get Started for Free</Link>
        </div>
      </section>

      {/* Footer */}
      <footer>
        <div className="container">
          <div className="footer-grid">
            <div className="footer-col">
              <h3>PronounceIT</h3>
              <p>Making education fun and engaging for children around the world.</p>
            </div>
            <div className="footer-col">
              <h4>Links</h4>
              <ul>
                <li><a href="#features">Features</a></li>
                <li><Link to="/pricing">Pricing</Link></li>
                <li><a href="#testimonials">Testimonials</a></li>
                <li><a href="#faq">FAQ</a></li>
              </ul>
            </div>
            <div className="footer-col">
              <h4>Support</h4>
              <ul>
                <li><Link to="/help">Help Center</Link></li>
                <li><Link to="/contact">Contact Us</Link></li>
                <li><Link to="/privacy">Privacy Policy</Link></li>
                <li><Link to="/terms">Terms of Service</Link></li>
              </ul>
            </div>
            <div className="footer-col">
  <h4>Stay Connected</h4>
  <div className="social-links">
    <a href="#" className="social-icon">
      <FontAwesomeIcon icon={faFacebookF} />
    </a>
    <a href="#" className="social-icon">
      <FontAwesomeIcon icon={faTwitter} />
    </a>
    <a href="#" className="social-icon">
      <FontAwesomeIcon icon={faInstagram} />
    </a>
    <a href="#" className="social-icon">
      <FontAwesomeIcon icon={faYoutube} />
    </a>
  </div>
</div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2025 PronounceIT. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </>
  );
}

export default Home;