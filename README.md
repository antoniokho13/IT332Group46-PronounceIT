PronounceIT: AI-Driven Pronunciation E-Learning System

## 1. Project Overview
   
PronounceIT is an interactive, AI-driven educational application designed to assist kindergarten students (ages 3 to 5) in improving their English pronunciation and expanding their vocabulary. The system is intended to supplement, not replace, traditional classroom instruction.

The core functionality relies on integrating Google Cloud Speech-to-Text (STT) API for real-time pronunciation analysis and Google Cloud Text-to-Speech (TTS) API for providing correct audio playback. The application features a gamified learning environment, user profile management, lesson progression based on performance, and administrative dashboards for teachers to manage content and track student analytics.

Key Modules
1. User Authentication & Authorization (Student, Teacher, Admin Roles)
2. User Profile Management (Progress Tracking, Account Editing)
3. Gameplay (Category/Lesson Selection, Core Pronunciation Evaluation)
4. Teacher Management (Content CRUD: Categories, Lessons, Words, Analytics)
5. Admin Management (User Management, Achievement Management)


## 2. Technology Stack & Architectural Design
PronounceIT follows a Multi-Layered Architecture supporting both web and mobile clients.



## 2.1. Core Technologies

| Component            | Technology              | Specific Version / Implementation           | Notes                                                     |
|----------------------|--------------------------|----------------------------------------------|-----------------------------------------------------------|
| **Backend**          | Spring Boot (Java)       | JPA, Hibernate                               | Handles business logic, security, and API management.     |
| **Frontend (Web)**   | React.js                 | Material UI                                  | Teacher and Admin dashboards.                             |
| **Frontend (Mobile)**| Kotlin                   | Android Activity, Jetpack components         | Primary client for student gameplay.                      |
| **Database**         | MySQL                    | Railway-hosted relational database           | Persistent storage for user data, progress, and content.  |
| **Security**         | Spring Security          | JWT Authentication                           | Role-based access control (RBAC) and session management.  |



## 2.2. Critical External Services

| Service           | API Used                                | Purpose                                                                 |
|-------------------|-------------------------------------------|-------------------------------------------------------------------------|
| **Speech Analysis** | Google Cloud Speech-to-Text (STT)         | Real-time transcription of student audio for pronunciation scoring.     |
| **Audio Guidance**  | Google Cloud Text-to-Speech (TTS)         | Generates clear audio for correct word pronunciations.                  |
| **Storage**         | Cloudify (Cloud Storage)                  | Content delivery for images, audio files, and lesson metadata.          |


## 2.3. Non-Functional Requirements Summary

- **Performance:** Pronunciation analysis must complete within **3 seconds**. General app response time ≤ **3 seconds**.

- **Accuracy:** Google Cloud STT accuracy target is ≥ **75%** for pronunciation validation.

- **Reliability:** System target is **99.5% uptime**. Failsafe mechanism implemented to retry failed API requests **3 times**.

- **Security & Compliance:** Must comply with **COPPA** and **GDPR-K**. Uses **JWT** for secure authentication. **No PII** (Personally Identifiable Information) is stored.

## 3. Deployment Instructions 
The system is designed for a layered deployment approach, separating the backend API and the frontend clients.

## 3.1. Prerequisites
1. Java Environment: Java Development Kit (JDK) for Spring Boot backend.
2. Node.js/npm: For building the React frontend.
3. MySQL Client: For database access and management.
4. Google Cloud Access: Credentials for STT, TTS, and Cloudify (storage) must be configured in the backend environment variables.


## 3.2. Backend Deployment (Spring Boot / Java)
The backend handles the core logic, security, and integration with Google Cloud APIs.

## Configuration
Create application.properties (or equivalent .env file) with necessary secrets:

## Example Backend Configuration

| Setting                                  | Value / Description |
|------------------------------------------|---------------------|
| **spring.datasource.url**                | `[YOUR_MYSQL_URL]` |
| **spring.datasource.username**           | `[DB_USER]` |
| **spring.datasource.password**           | `[DB_PASSWORD]` |
| **jwt.secret**                            | `[YOUR_LONG_JWT_SECRET_KEY]` |
| **google.cloud.api.key**                 | `[YOUR_GC_API_KEY]` |
| **Service Account File (Optional)**      | Point to a mounted GCP service account file for secure deployments |


## Build & Run Commands

| Action                  | Command |
|-------------------------|---------|
| **Build the JAR file**  | `./gradlew clean build` |
| **Run the application** | `java -jar build/libs/[app-name].jar` |


## 3.3 Frontend Deployment (React Web & Kotlin Mobile)

## Web Frontend (Teacher/Admin Dashboard)
1. Navigate to the web project directory: **cd frontend-web**
2. install dependencies: **npm install**
3. Set the API URL environment variable:

## Example `.env` Configuration for React

| Variable              | Value / Description                    |
|-----------------------|-----------------------------------------|
| **REACT_APP_API_URL** | `[http://your-backend-api-url]/api`     |

4. Build and serve the application:
   
                  
## Frontend Build & Deployment Commands

| Action                                | Command / Description |
|----------------------------------------|-------------------------|
| **Build the production bundle**        | `npm run` |


## Mobile Frontend (Student App)
1. Opent the project in Android Studio.
2. Ensure Kotlin/Jetpack dependencies are up-to-date
3. configure the backend API URL in the mobile application's config files
4. build and deploy the APK to target devices


## 4. Sample Credentials (Development/Testing Only)

These credentials are used for local testing and validation as detailed in the Software Test Document.

| User Type            | Email (Username)              | Password          | Notes                                                           |
|----------------------|-------------------------------|-------------------|-----------------------------------------------------------------|
| **Admin**            | admin@[project-domain].com     | SecureAdmin123!   | Used for Achievement and User Management.                       |
| **Teacher**          | michael.johnson@school.edu     | TeacherPass456@   | Used for Category, Lesson, Word CRUD, and Student Analytics.    |
| **Student**          | emma.wilson@test.com           | TestPass123!      | Used for Gameplay, Pronunciation Practice, and Score Tracking.  |




