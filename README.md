Learning Management System (LMS) - Java
A console-based Learning Management System built entirely in pure Java (JDK 17+). No external libraries or database are required.
Features
Admin
- View all users and courses.
- Add new instructors.
- Remove courses.
Instructor
- Create courses.
- Add course materials.
- Create assignments with maximum marks.
- View student submissions and assign marks with feedback.
- View the list of enrolled students.
Student
- Self-registration.
- Browse available courses and enroll in them.
- View course content (materials and assignments).
- Submit assignments.
- View personal grades and percentages.
Technical Features
- Role-based login (ADMIN / INSTRUCTOR / STUDENT).
- Passwords are stored using SHA-256 hashing.
- Data is automatically saved to data/lms.dat using Java Serialization.
- Data persists even after restarting the application.
- Object-oriented design with model, service, and util packages.
Project Structure
LMS/
├── src/com/lms/
│   ├── Main.java                 # Console menus (UI)
│   ├── model/                    # User, Role, Course, Assignment, Submission
│   ├── service/                  # LMSService (business logic), LMSException
│   └── util/                     # DataStore (save/load), PasswordUtil
├── run.sh                        # Compile and run (Linux / Mac)
├── run.bat                       # Compile and run (Windows)
└── README.md


How to Run
JDK 17 or above must be installed.

Check your Java version using:
java -version


Linux / Mac
chmod +x run.sh
./run.sh


Windows
run.bat


Manual Execution
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.lms.Main


Demo Login Credentials
Role	Email	Password
Admin	admin@lms.com	admin123
Instructor	teacher@lms.com	teach123
Student	student@lms.com	stud123

Reset Data
To reset all application data, simply delete the data/ folder.

Future Enhancements
- Integrate JDBC with MySQL for persistent database storage.
- Develop a graphical user interface (GUI) using Swing or JavaFX.
- Add features such as quizzes, attendance tracking, and certificate generation.
- Develop a Spring Boot REST API for backend integration and web application support.
