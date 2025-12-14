# 📘 PronounceIT: AI-Driven Pronunciation E-Learning System

## 1. Project Overview

**PronounceIT** is an interactive, AI-driven educational application designed to assist kindergarten students (ages 3 to 5) in improving their English pronunciation and expanding their vocabulary. The system is intended to supplement, not replace, traditional classroom instruction.

The core functionality relies on the integration of **Google Cloud Speech-to-Text (STT) API** for real-time pronunciation analysis and **Google Cloud Text-to-Speech (TTS) API** for providing correct audio playback. The application features a gamified learning environment, user profile management, lesson progression based on learner performance, and administrative dashboards for teachers to manage content and track student analytics.

### Key Modules
- User Authentication & Authorization (Student, Teacher, Admin roles)
- User Profile Management (Progress tracking, account editing)
- Gameplay Module (Category and lesson selection, pronunciation evaluation)
- Teacher Management Module
  - CRUD operations for categories, lessons, and words
  - Student analytics and performance tracking
- Admin Management Module
  - User account management
  - Achievement and system data management

---

## 2. Complete Technology Stack

### Frontend (Web Application)
- React.js – v18.x  
- Create React App – v5.x  
- Node.js – v18.x  
- npm – v9.x  
- HTML5 / CSS3 / JavaScript (ES6+)

### Mobile Application
- Android Studio – Latest stable release  
- Java – JDK 17  
- Android SDK – API Level 33+

### Backend
- Spring Boot – v3.x  
- Java – JDK 17  
- Spring Security – Authentication and role-based authorization  
- Spring Data JPA – ORM and database interaction  
- RESTful API Architecture

### Database
- MySQL – v8.0  
- Hibernate ORM

### Cloud & APIs
- Google Cloud Speech-to-Text API  
- Google Cloud Text-to-Speech API

### Development & Tools
- Git & GitHub – Source code version control  
- Postman – API testing  
- VS Code / IntelliJ IDEA – Development IDEs

---

## 3. Deployment Instructions

### 3.1 Frontend Deployment (React)

#### Prerequisites
- Node.js v18+
- npm installed

#### Steps
1. Install dependencies:
   npm install
2. Start the development server:
   npm start

The frontend application will run at:
http://localhost:3000

To create a production build:
npm run build

---

### 3.2 Backend Deployment (Spring Boot)

#### Prerequisites
- Java JDK 17
- MySQL 8.0
- Maven

#### Steps
1. Open the backend project in IntelliJ IDEA or Eclipse
2. Configure the database connection in application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/pronounceit_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update

3. Run the backend server:
   mvn spring-boot:run

The backend API will be available at:
http://localhost:8080

---

### 3.3 Mobile Application Deployment (Android)

1. Open Android Studio
2. Select Open Existing Project
3. Sync Gradle files
4. Run the application using:
   - Android Emulator, or
   - Physical Android device with USB debugging enabled

---

## 4. Sample / Dummy User Accounts (Existing Server)

The following test accounts are provided for system evaluation and demonstration purposes:

Admin  
Username: admin@gmail.com  
Password: admin123  

Teacher  
Username: teacher1@gmail.com  
Password: teacher1  

Student  
Username: student1@gmail.com  
Password: student1  

Note: These credentials are for academic testing and evaluation purposes only.

---

## 5. Database Export / Dump

Database Name: pronounceit_db  
Database Engine: MySQL 8.0  
Export Format: .sql  

### Restore Instructions
mysql -u root -p pronounceit_db < pronounceit_db.sql

The database dump includes:
- User accounts
- Categories, lessons, and vocabulary words
- Student pronunciation records
- Progress tracking and analytics data

---

## 6. Notes for Evaluators

- Internet connection is required for Google Cloud STT/TTS services
- Microphone permission must be enabled for pronunciation evaluation
- Default system ports:
  - Frontend: 3000
  - Backend: 8080
  - MySQL: 3306

---
