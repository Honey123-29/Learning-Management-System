# Learning Management System (LMS) - Java

Ek console-based Learning Management System, pure Java (JDK 17+) mein. Koi external library ya database nahi chahiye.

## Features

**Admin**
- Saare users aur courses dekhna
- Naye instructor add karna
- Course remove karna

**Instructor**
- Course create karna
- Course material add karna
- Assignment banana (max marks ke saath)
- Student submissions dekhna aur marks + feedback dena
- Enrolled students ki list

**Student**
- Self registration
- Courses browse aur enroll karna
- Course content (materials + assignments) dekhna
- Assignment submit karna
- Apne grades aur percentage dekhna

**Technical**
- Role-based login (ADMIN / INSTRUCTOR / STUDENT)
- Passwords SHA-256 hash ke saath store hote hain
- Data automatically `data/lms.dat` mein save hota hai (Java serialization), restart ke baad bhi rehta hai
- OOP design: `model`, `service`, `util` packages

## Project Structure

```
LMS/
├── src/com/lms/
│   ├── Main.java                 # console menus (UI)
│   ├── model/                    # User, Role, Course, Assignment, Submission
│   ├── service/                  # LMSService (business logic), LMSException
│   └── util/                     # DataStore (save/load), PasswordUtil
├── run.sh                        # compile + run (Linux / Mac)
├── run.bat                       # compile + run (Windows)
└── README.md
```

## Run kaise karein

JDK 17 ya usse upar install hona chahiye (`java -version` se check karein).

**Linux / Mac**
```bash
chmod +x run.sh
./run.sh
```

**Windows**
```
run.bat
```

**Manually**
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.lms.Main
```

## Demo Logins

| Role       | Email             | Password |
|------------|-------------------|----------|
| Admin      | admin@lms.com     | admin123 |
| Instructor | teacher@lms.com   | teach123 |
| Student    | student@lms.com   | stud123  |

Data reset karna ho to `data/` folder delete kar dein.

## Aage badhane ke ideas

- JDBC + MySQL se persistent database
- Swing / JavaFX GUI
- Quizzes, attendance, certificates
- Spring Boot REST API
