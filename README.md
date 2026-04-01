# AttendX — Backend Setup & Deployment Guide

## Stack
- **Backend:** Java 17 + Spring Boot 3.2 + MongoDB
- **Frontend:** React + Vite (Vercel)
- **Backend Hosting:** Render
- **Database:** MongoDB Atlas

---

## 1. Prerequisites — Install These First

| Tool | Version | Download |
|------|---------|---------|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| MongoDB | Local or Atlas | https://www.mongodb.com/atlas |

Verify installs:
```bash
java -version       # should show 17+
mvn -version        # should show 3.8+
```

---

## 2. Local Development Setup

### Step 1 — Clone / Place the backend folder
```bash
cd attendx-backend
```

### Step 2 — Configure environment
Edit `src/main/resources/application.properties`:
```properties
# For local MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/attendx

# OR for MongoDB Atlas (recommended)
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.mongodb.net/attendx

jwt.secret=your-secret-key-at-least-32-chars-long
cors.allowed-origins=http://localhost:5173
```

### Step 3 — Run the backend
```bash
mvn spring-boot:run
```

Backend starts at **http://localhost:8080**

On first run, the `DataSeeder` automatically inserts:
- 1 Admin, 7 Faculty, 59 Students into MongoDB
- 7 Subjects, 1 Department, 11 Timetable slots

### Step 4 — Test login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@college.edu","password":"admin123","role":"admin"}'
```

---

## 3. Frontend — Connect to Backend

### Step 1 — Add `.env` file in your frontend root
```env
VITE_API_URL=http://localhost:8080/api
```

### Step 2 — Replace these files in your frontend `src/`:

| Replace | With file from |
|---------|---------------|
| `src/services/api.js` | `FRONTEND_UPDATES/services/api.js` |
| `src/context/AuthContext.jsx` | `FRONTEND_UPDATES/context/AuthContext.jsx` |
| `src/context/NotificationContext.jsx` | `FRONTEND_UPDATES/context/NotificationContext.jsx` |
| `src/services/authService.js` | `FRONTEND_UPDATES/services/authService.js` |
| `src/services/attendanceService.js` | `FRONTEND_UPDATES/services/attendanceService.js` |
| `src/services/userService.js` | `FRONTEND_UPDATES/services/userService.js` |
| `src/services/reportService.js` | `FRONTEND_UPDATES/services/reportService.js` |

### Step 3 — Add new file
Copy `FRONTEND_UPDATES/services/qrService.js` → `src/services/qrService.js`

---

## 4. Deploy Backend to Render

### Step 1 — Push to GitHub
```bash
git init
git add .
git commit -m "AttendX Spring Boot backend"
git remote add origin https://github.com/YOUR_USERNAME/attendx-backend.git
git push -u origin main
```

### Step 2 — Create Render Web Service
1. Go to https://render.com → New → Web Service
2. Connect your GitHub repo
3. Settings:
   - **Environment:** Java
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar target/attendx-backend-1.0.0.jar`
   - **Instance Type:** Free (for testing)

### Step 3 — Add Environment Variables on Render
| Key | Value |
|-----|-------|
| `MONGODB_URI` | Your MongoDB Atlas connection string |
| `JWT_SECRET` | A strong random string (32+ chars) |
| `FRONTEND_URL` | Your Vercel app URL (e.g. https://attendx.vercel.app) |

### Step 4 — Update application.properties for production
The file already reads from env variables:
```properties
spring.data.mongodb.uri=${MONGODB_URI}
jwt.secret=${JWT_SECRET}
cors.allowed-origins=${FRONTEND_URL}
```

---

## 5. Deploy Frontend to Vercel

### Step 1 — Add environment variable in Vercel dashboard
```
VITE_API_URL = https://your-render-app.onrender.com/api
```

### Step 2 — Redeploy
Vercel will pick up the new env variable automatically.

---

## 6. API Endpoints Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login (email, password, role) |
| GET | `/api/auth/me` | Get current user from JWT |
| POST | `/api/auth/forgot-password` | Send reset email |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | All users (admin) |
| GET | `/api/users/students` | All 59 students |
| GET | `/api/users/faculty` | All faculty |
| POST | `/api/users` | Create user (admin) |
| PUT | `/api/users/{id}` | Update user (admin) |
| DELETE | `/api/users/{id}` | Delete user (admin) |

### Attendance
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/attendance/mark` | Mark attendance (faculty) |
| GET | `/api/attendance/student/{id}/stats` | Student subject-wise stats |
| GET | `/api/attendance/student/{id}/records` | Student daily records |
| GET | `/api/attendance/subject/{id}/history` | Subject attendance history |
| GET | `/api/attendance/overall/{studentId}` | Overall % for student |
| GET | `/api/attendance/analytics/summary` | Dashboard summary |
| GET | `/api/attendance/analytics/subject-wise` | Subject-wise chart data |
| GET | `/api/attendance/analytics/student-wise` | Student-wise chart data |
| GET | `/api/attendance/analytics/batch-wise` | Batch-wise chart data |

### QR Sessions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/qr/start` | Start QR session (faculty) |
| POST | `/api/qr/end` | End session (faculty) |
| GET | `/api/qr/active` | Get active session (student polls) |
| POST | `/api/qr/mark` | Student marks present with PIN |
| GET | `/api/qr/history/faculty/{id}` | Session history |

### Subjects / Timetable / Departments / Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/subjects` | All subjects |
| GET | `/api/subjects/faculty/{id}` | Faculty's subjects |
| GET | `/api/timetable` | All timetable slots |
| GET | `/api/timetable/faculty/{id}` | Faculty timetable |
| GET | `/api/timetable/class/{class}` | Student timetable |
| POST | `/api/timetable` | Add slot (admin) |
| PUT | `/api/timetable/{id}` | Edit slot |
| DELETE | `/api/timetable/{id}` | Delete slot |
| GET | `/api/departments` | All departments |
| POST | `/api/departments` | Add department (admin) |
| DELETE | `/api/departments/{id}` | Delete department |
| GET | `/api/notifications` | My notifications |
| GET | `/api/notifications/unread-count` | Unread badge count |
| PATCH | `/api/notifications/{id}/read` | Mark single read |
| PATCH | `/api/notifications/mark-all-read` | Mark all read |

---

## 7. Default Login Credentials

| Role | Email | Password |
|------|-------|---------|
| Admin | admin@college.edu | admin123 |
| Faculty (IOT) | anita@college.edu | faculty123 |
| Faculty (DSE) | vikram@college.edu | faculty123 |
| Faculty (TCS) | priya@college.edu | faculty123 |
| Faculty (CNS) | suresh@college.edu | faculty123 |
| Faculty (FM) | meera@college.edu | faculty123 |
| Faculty (PM) | rahul@college.edu | faculty123 |
| Faculty (COI) | sneha@college.edu | faculty123 |
| Student (A1) | arjun@student.edu | student123 |
| Student (A2) | aryaman@student.edu | student123 |
| Student (A3) | dhruvi@student.edu | student123 |

---

## 8. Project Structure

```
attendx-backend/
├── pom.xml
├── src/main/java/com/attendx/
│   ├── AttendXApplication.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Subject.java
│   │   ├── Department.java
│   │   ├── TimetableSlot.java
│   │   ├── AttendanceRecord.java
│   │   ├── QRSession.java
│   │   └── Notification.java
│   ├── repository/         (MongoRepository interfaces)
│   ├── controller/         (REST endpoints)
│   ├── dto/                (Request/Response objects)
│   ├── security/           (JWT utils + filter)
│   └── config/
│       ├── SecurityConfig.java   (CORS + Spring Security)
│       └── DataSeeder.java       (Seeds 59 students on startup)
├── src/main/resources/
│   └── application.properties
└── FRONTEND_UPDATES/       (Updated frontend files)
    ├── context/
    │   ├── AuthContext.jsx
    │   └── NotificationContext.jsx
    └── services/
        ├── api.js
        ├── authService.js
        ├── attendanceService.js
        ├── userService.js
        ├── reportService.js
        └── qrService.js (NEW)
```
