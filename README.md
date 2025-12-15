# PronounceIT  
### English Vocabulary & Pronunciation E-Learning System for Kindergarten

PronounceIT is an interactive, AI-driven educational application designed to assist kindergarten students (ages 3–5) in improving their English pronunciation and expanding their vocabulary. The system is intended to supplement, not replace, traditional classroom instruction.

---

## Tech Stack

### Web Frontend
| Technology | Version / Release |
|----------|------------------|
| React.js | 18.x |
| Node.js | 18.x |
| CSS | 3 (CSS3) |
| React Router | 6.x |
| Axios | 1.x |

---

### Mobile Frontend
| Technology | Version / Release |
|----------|------------------|
| Android Studio | Latest Stable Release |
| Java | 17 |
| Android SDK | API Level 33+ |

---

### Backend
| Technology | Version / Release |
|----------|------------------|
| Spring Boot | 3.x |
| Java | 17 |
| Spring Security | 6.x |
| JWT (JSON Web Tokens) | Latest |
| MySQL | 8.x |
| Railway (Backend Hosting) | Hobby Tier (USD $5 minimum usage) |
| Railway – MySQL (Database) | Hobby Tier (USD $5 minimum usage) |

---

### Development & Tools
| Technology | Version / Release |
|----------|------------------|
| Postman | Latest |
| GitHub | Latest |
| Vercel (Frontend Hosting) | Latest |

---

## Deployment Instructions

Below are the steps to deploy both frontend and backend based on the current repository structure.

---

## Backend Deployment (Spring Boot + Railway)

### 1. Clone the Repository
```bash
git clone https://github.com/antoniukho13/IT332Group46-PronounceIT.git
cd IT332Group46-PronounceIT/backend
```

---

### 2. Configure application.properties
```properties
spring.application.name=PronounceIT
spring.datasource.url=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
spring.datasource.username=${MYSQLUSER}
spring.datasource.password=${MYSQLPASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

spring.web.resources.static-locations=file:/app/uploads/,classpath:/static/

server.address=0.0.0.0
server.port=${PORT:8080}

spring.cloud.gcp.credentials.location=file:${GOOGLE_APPLICATION_CREDENTIALS}
```

---

### 3. Set Up Railway (Backend + Database)
1. Go to Railway → Create a New Project  
2. Click Add → Database → MySQL  
3. Copy generated database variables  

---

### 4. Configure Railway Environment Variables
```
MYSQLHOST
MYSQLPORT
MYSQLDATABASE
MYSQLUSER
MYSQLPASSWORD
JWT_SECRET
JWT_EXPIRATION
GOOGLE_APPLICATION_CREDENTIALS=/app/credentials.json
GOOGLE_CREDENTIALS_JSON
```

---

### 5. Deploy Backend on Railway
- Add GitHub Repo in Railway  
- Set Root Directory to `backend`  
- Deploy automatically  

---

## Frontend Deployment (React + Vercel)

```bash
cd frontend
npm install
npm start
```

Deploy using:
```bash
vercel
```

---

## Sample User Accounts

**Student**  
Email: student1@gmail.com  
Password: student1  

**Teacher**  
Email: teacher1@gmail.com  
Password: teacher1  

**Admin**  
Email: admin@gmail.com  
Password: admin123  

---

## License
Academic and research use only.
