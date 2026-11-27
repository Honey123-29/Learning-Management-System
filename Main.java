package com.lms;

import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.Role;
import com.lms.model.Submission;
import com.lms.model.User;
import com.lms.service.LMSException;
import com.lms.service.LMSService;
import com.lms.util.DataStore;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Console UI for the Learning Management System. */
public class Main {
    private static final Scanner IN = new Scanner(System.in);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    private static LMSService lms;

    public static void main(String[] args) {
        lms = DataStore.load();
        if (lms == null) {
            lms = new LMSService();
            lms.seedDemoData();
            save();
        }

        System.out.println("==========================================");
        System.out.println("   LEARNING MANAGEMENT SYSTEM (LMS)");
        System.out.println("==========================================");
        System.out.println("Demo logins:");
        System.out.println("  Admin      : admin@lms.com   / admin123");
        System.out.println("  Instructor : teacher@lms.com / teach123");
        System.out.println("  Student    : student@lms.com / stud123");

        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Login");
            System.out.println("2. Register as Student");
            System.out.println("0. Exit");
            String choice = ask("Choose");
            try {
                switch (choice) {
                    case "1" -> login();
                    case "2" -> registerStudent();
                    case "0" -> {
                        save();
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (LMSException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ auth

    private static void registerStudent() {
        String name = ask("Full name");
        String email = ask("Email");
        String password = ask("Password (min 4 chars)");
        User u = lms.register(name, email, password, Role.STUDENT);
        save();
        System.out.println("Registered! Your student ID is " + u.getId() + ". You can login now.");
    }

    private static void login() {
        String email = ask("Email");
        String password = ask("Password");
        User user = lms.login(email, password);
        if (user == null) {
            System.out.println("Wrong email or password.");
            return;
        }
        System.out.println("\nWelcome, " + user.getName() + " (" + user.getRole() + ")");
        switch (user.getRole()) {
            case ADMIN -> adminMenu(user);
            case INSTRUCTOR -> instructorMenu(user);
            case STUDENT -> studentMenu(user);
        }
    }

    // ----------------------------------------------------------- admin

    private static void adminMenu(User admin) {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. View all users");
            System.out.println("2. View all courses");
            System.out.println("3. Add instructor");
            System.out.println("4. Remove a course");
            System.out.println("0. Logout");
            try {
                switch (ask("Choose")) {
                    case "1" -> {
                        System.out.println(String.format("%-6s %-20s %-28s %s", "ID", "Name", "Email", "Role"));
                        for (User u : lms.allUsers()) System.out.println(u);
                    }
                    case "2" -> printCourses(lms.allCourses());
                    case "3" -> {
                        String name = ask("Instructor name");
                        String email = ask("Email");
                        String pass = ask("Password (min 4 chars)");
                        User u = lms.register(name, email, pass, Role.INSTRUCTOR);
                        System.out.println("Instructor created with ID " + u.getId());
                    }
                    case "4" -> {
                        printCourses(lms.allCourses());
                        lms.removeCourse(ask("Course ID to remove"));
                        System.out.println("Course removed.");
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
                save();
            } catch (LMSException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------ instructor

    private static void instructorMenu(User teacher) {
        while (true) {
            System.out.println("\n--- INSTRUCTOR MENU ---");
            System.out.println("1. Create course");
            System.out.println("2. My courses");
            System.out.println("3. Add course material");
            System.out.println("4. Add assignment");
            System.out.println("5. View submissions and grade");
            System.out.println("6. View enrolled students");
            System.out.println("0. Logout");
            try {
                switch (ask("Choose")) {
                    case "1" -> {
                        String title = ask("Course title");
                        String desc = ask("Description");
                        Course c = lms.createCourse(teacher, title, desc);
                        System.out.println("Course created with ID " + c.getId());
                    }
                    case "2" -> printCourses(lms.coursesOfInstructor(teacher.getId()));
                    case "3" -> {
                        printCourses(lms.coursesOfInstructor(teacher.getId()));
                        String cid = ask("Course ID");
                        lms.addMaterial(teacher, cid, ask("Material (title / link / note)"));
                        System.out.println("Material added.");
                    }
                    case "4" -> {
                        printCourses(lms.coursesOfInstructor(teacher.getId()));
                        String cid = ask("Course ID");
                        String title = ask("Assignment title");
                        String desc = ask("Description");
                        int marks = askInt("Max marks");
                        Assignment a = lms.addAssignment(teacher, cid, title, desc, marks);
                        System.out.println("Assignment created with ID " + a.getId());
                    }
                    case "5" -> gradeFlow(teacher);
                    case "6" -> {
                        printCourses(lms.coursesOfInstructor(teacher.getId()));
                        Course c = lms.getOwnedCourse(teacher, ask("Course ID"));
                        if (c.getStudentIds().isEmpty()) System.out.println("No students enrolled yet.");
                        for (String sid : c.getStudentIds()) {
                            User s = lms.getStudent(sid);
                            System.out.println(s.getId() + "  " + s.getName() + "  (" + s.getEmail() + ")");
                        }
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
                save();
            } catch (LMSException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void gradeFlow(User teacher) {
        printCourses(lms.coursesOfInstructor(teacher.getId()));
        Course c = lms.getOwnedCourse(teacher, ask("Course ID"));
        if (c.getAssignments().isEmpty()) {
            System.out.println("This course has no assignments.");
            return;
        }
        for (Assignment a : c.getAssignments()) System.out.println(a);
        Assignment a = lms.getAssignment(c, ask("Assignment ID"));

        if (a.getSubmissions().isEmpty()) {
            System.out.println("No submissions yet.");
            return;
        }
        System.out.println("\nSubmissions for " + a.getTitle() + ":");
        for (Submission s : a.getSubmissions()) {
            String state = s.isGraded() ? "graded " + s.getMarks() + "/" + a.getMaxMarks() : "not graded";
            System.out.println("  " + s.getStudentId() + " " + lms.userName(s.getStudentId())
                    + " | " + s.getSubmittedAt().format(FMT) + " | " + state);
            System.out.println("     Answer: " + s.getContent());
        }
        String sid = ask("\nStudent ID to grade (blank to cancel)");
        if (sid.isBlank()) return;
        int marks = askInt("Marks (0-" + a.getMaxMarks() + ")");
        String feedback = ask("Feedback");
        lms.grade(teacher, c.getId(), a.getId(), sid, marks, feedback);
        System.out.println("Graded successfully.");
    }

    // --------------------------------------------------------- student

    private static void studentMenu(User student) {
        while (true) {
            System.out.println("\n--- STUDENT MENU ---");
            System.out.println("1. Browse all courses");
            System.out.println("2. Enroll in a course");
            System.out.println("3. My courses");
            System.out.println("4. Course content (materials and assignments)");
            System.out.println("5. Submit assignment");
            System.out.println("6. My grades");
            System.out.println("0. Logout");
            try {
                switch (ask("Choose")) {
                    case "1" -> printCourses(lms.allCourses());
                    case "2" -> {
                        printCourses(lms.allCourses());
                        String cid = ask("Course ID to enroll");
                        lms.enroll(student, cid);
                        System.out.println("Enrolled successfully!");
                    }
                    case "3" -> printCourses(lms.coursesOfStudent(student.getId()));
                    case "4" -> {
                        printCourses(lms.coursesOfStudent(student.getId()));
                        Course c = lms.getCourse(ask("Course ID"));
                        if (!c.isEnrolled(student.getId())) throw new LMSException("You are not enrolled in this course.");
                        showCourseContent(c, student);
                    }
                    case "5" -> {
                        printCourses(lms.coursesOfStudent(student.getId()));
                        Course c = lms.getCourse(ask("Course ID"));
                        if (!c.isEnrolled(student.getId())) throw new LMSException("You are not enrolled in this course.");
                        showCourseContent(c, student);
                        String aid = ask("Assignment ID");
                        String answer = ask("Your answer / submission text");
                        lms.submit(student, c.getId(), aid, answer);
                        System.out.println("Submitted!");
                    }
                    case "6" -> {
                        System.out.println();
                        lms.gradeReport(student).forEach(System.out::println);
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
                save();
            } catch (LMSException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void showCourseContent(Course c, User student) {
        System.out.println("\n=== " + c.getTitle() + " (taught by " + lms.userName(c.getInstructorId()) + ") ===");
        System.out.println("Materials:");
        if (c.getMaterials().isEmpty()) System.out.println("  (none)");
        int i = 1;
        for (String m : c.getMaterials()) System.out.println("  " + (i++) + ". " + m);
        System.out.println("Assignments:");
        if (c.getAssignments().isEmpty()) System.out.println("  (none)");
        for (Assignment a : c.getAssignments()) {
            Submission s = a.getSubmission(student.getId());
            String state = s == null ? "pending" : (s.isGraded() ? "graded" : "submitted");
            System.out.println("  " + a + "  [" + state + "]");
            if (!a.getDescription().isEmpty()) System.out.println("      " + a.getDescription());
        }
    }

    // --------------------------------------------------------- helpers

    private static void printCourses(Collection<Course> courses) {
        if (courses.isEmpty()) {
            System.out.println("(no courses)");
            return;
        }
        System.out.println(String.format("%-6s %-24s %-16s %-8s %s", "ID", "Title", "Instructor", "Students", "Description"));
        for (Course c : courses) {
            System.out.println(String.format("%-6s %-24s %-16s %-8d %s", c.getId(), c.getTitle(),
                    lms.userName(c.getInstructorId()), c.getStudentIds().size(), c.getDescription()));
        }
    }

    private static String ask(String prompt) {
        System.out.print(prompt + ": ");
        try {
            return IN.nextLine().trim();
        } catch (NoSuchElementException e) { // input closed (Ctrl+D)
            save();
            System.out.println("\nGoodbye!");
            System.exit(0);
            return "";
        }
    }

    private static int askInt(String prompt) {
        String text = ask(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new LMSException("Please enter a valid number.");
        }
    }

    private static void save() {
        DataStore.save(lms);
    }
}
