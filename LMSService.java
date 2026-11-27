package com.lms.service;

import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.Role;
import com.lms.model.Submission;
import com.lms.model.User;
import com.lms.util.PasswordUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** All the business logic of the Learning Management System. */
public class LMSService implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Course> courses = new LinkedHashMap<>();
    private int userSeq = 1;
    private int courseSeq = 1;
    private int assignmentSeq = 1;

    // ---------------------------------------------------------------- users

    public User register(String name, String email, String password, Role role) {
        if (name == null || name.isBlank()) throw new LMSException("Name cannot be empty.");
        if (email == null || !email.contains("@")) throw new LMSException("Enter a valid email.");
        if (password == null || password.length() < 4) throw new LMSException("Password must be at least 4 characters.");
        String mail = email.trim().toLowerCase();
        for (User u : users.values()) {
            if (u.getEmail().equals(mail)) throw new LMSException("This email is already registered.");
        }
        String prefix = switch (role) {
            case ADMIN -> "A";
            case INSTRUCTOR -> "I";
            case STUDENT -> "S";
        };
        String id = prefix + String.format("%03d", userSeq++);
        User user = new User(id, name.trim(), mail, PasswordUtil.hash(password), role);
        users.put(id, user);
        return user;
    }

    /** Returns the user when credentials are right, otherwise null. */
    public User login(String email, String password) {
        String mail = email == null ? "" : email.trim().toLowerCase();
        String hash = PasswordUtil.hash(password == null ? "" : password);
        for (User u : users.values()) {
            if (u.getEmail().equals(mail) && u.getPasswordHash().equals(hash)) return u;
        }
        return null;
    }

    public Collection<User> allUsers() { return users.values(); }

    public String userName(String userId) {
        User u = users.get(userId);
        return u == null ? "(unknown)" : u.getName();
    }

    public User getStudent(String studentId) {
        User u = users.get(studentId);
        if (u == null || u.getRole() != Role.STUDENT) throw new LMSException("Student not found: " + studentId);
        return u;
    }

    // -------------------------------------------------------------- courses

    public Course createCourse(User instructor, String title, String description) {
        if (title == null || title.isBlank()) throw new LMSException("Course title cannot be empty.");
        String id = "C" + String.format("%03d", courseSeq++);
        Course c = new Course(id, title.trim(), description == null ? "" : description.trim(), instructor.getId());
        courses.put(id, c);
        return c;
    }

    public Course getCourse(String courseId) {
        Course c = courses.get(courseId == null ? "" : courseId.trim().toUpperCase());
        if (c == null) throw new LMSException("Course not found: " + courseId);
        return c;
    }

    /** Course that must belong to the given instructor. */
    public Course getOwnedCourse(User instructor, String courseId) {
        Course c = getCourse(courseId);
        if (!c.getInstructorId().equals(instructor.getId())) {
            throw new LMSException("You can only manage your own courses.");
        }
        return c;
    }

    public Collection<Course> allCourses() { return courses.values(); }

    public List<Course> coursesOfInstructor(String instructorId) {
        List<Course> list = new ArrayList<>();
        for (Course c : courses.values()) {
            if (c.getInstructorId().equals(instructorId)) list.add(c);
        }
        return list;
    }

    public List<Course> coursesOfStudent(String studentId) {
        List<Course> list = new ArrayList<>();
        for (Course c : courses.values()) {
            if (c.isEnrolled(studentId)) list.add(c);
        }
        return list;
    }

    public void removeCourse(String courseId) {
        Course c = getCourse(courseId);
        courses.remove(c.getId());
    }

    public void enroll(User student, String courseId) {
        Course c = getCourse(courseId);
        if (c.isEnrolled(student.getId())) throw new LMSException("You are already enrolled in " + c.getTitle() + ".");
        c.getStudentIds().add(student.getId());
    }

    public void addMaterial(User instructor, String courseId, String material) {
        if (material == null || material.isBlank()) throw new LMSException("Material cannot be empty.");
        getOwnedCourse(instructor, courseId).getMaterials().add(material.trim());
    }

    // ---------------------------------------------------------- assignments

    public Assignment addAssignment(User instructor, String courseId, String title, String description, int maxMarks) {
        if (title == null || title.isBlank()) throw new LMSException("Assignment title cannot be empty.");
        if (maxMarks <= 0) throw new LMSException("Max marks must be greater than 0.");
        Course c = getOwnedCourse(instructor, courseId);
        String id = "AS" + String.format("%03d", assignmentSeq++);
        Assignment a = new Assignment(id, title.trim(), description == null ? "" : description.trim(), maxMarks);
        c.addAssignment(a);
        return a;
    }

    public Assignment getAssignment(Course course, String assignmentId) {
        Assignment a = course.getAssignment(assignmentId == null ? "" : assignmentId.trim().toUpperCase());
        if (a == null) throw new LMSException("Assignment not found: " + assignmentId);
        return a;
    }

    public void submit(User student, String courseId, String assignmentId, String content) {
        if (content == null || content.isBlank()) throw new LMSException("Submission cannot be empty.");
        Course c = getCourse(courseId);
        if (!c.isEnrolled(student.getId())) throw new LMSException("You are not enrolled in this course.");
        Assignment a = getAssignment(c, assignmentId);
        Submission existing = a.getSubmission(student.getId());
        if (existing != null && existing.isGraded()) {
            throw new LMSException("This submission is already graded and cannot be changed.");
        }
        a.putSubmission(new Submission(student.getId(), content.trim()));
    }

    public void grade(User instructor, String courseId, String assignmentId, String studentId, int marks, String feedback) {
        Course c = getOwnedCourse(instructor, courseId);
        Assignment a = getAssignment(c, assignmentId);
        Submission s = a.getSubmission(studentId == null ? "" : studentId.trim().toUpperCase());
        if (s == null) throw new LMSException("No submission from student " + studentId + ".");
        if (marks < 0 || marks > a.getMaxMarks()) {
            throw new LMSException("Marks must be between 0 and " + a.getMaxMarks() + ".");
        }
        s.grade(marks, feedback == null ? "" : feedback.trim());
    }

    // -------------------------------------------------------------- reports

    /** One readable line per assignment for the student's progress / grades screen. */
    public List<String> gradeReport(User student) {
        List<String> lines = new ArrayList<>();
        for (Course c : coursesOfStudent(student.getId())) {
            lines.add("Course: " + c.getId() + " - " + c.getTitle());
            if (c.getAssignments().isEmpty()) lines.add("    (no assignments yet)");
            int got = 0;
            int total = 0;
            for (Assignment a : c.getAssignments()) {
                Submission s = a.getSubmission(student.getId());
                String status;
                if (s == null) {
                    status = "Not submitted";
                } else if (!s.isGraded()) {
                    status = "Submitted, waiting for grading";
                } else {
                    status = "Marks " + s.getMarks() + "/" + a.getMaxMarks();
                    if (!s.getFeedback().isEmpty()) status += "  | Feedback: " + s.getFeedback();
                    got += s.getMarks();
                    total += a.getMaxMarks();
                }
                lines.add(String.format("    %-7s %-24s %s", a.getId(), a.getTitle(), status));
            }
            if (total > 0) {
                lines.add(String.format("    Total graded: %d/%d (%.1f%%)", got, total, got * 100.0 / total));
            }
        }
        if (lines.isEmpty()) lines.add("You are not enrolled in any course yet.");
        return lines;
    }

    // ------------------------------------------------------------ demo data

    public void seedDemoData() {
        register("Admin", "admin@lms.com", "admin123", Role.ADMIN);
        User teacher = register("Rahul Sharma", "teacher@lms.com", "teach123", Role.INSTRUCTOR);
        User student = register("Aman Verma", "student@lms.com", "stud123", Role.STUDENT);

        Course java = createCourse(teacher, "Java Programming", "Core Java: OOP, collections, exceptions, file I/O");
        addMaterial(teacher, java.getId(), "Lecture 1 - Introduction to Java and the JVM");
        addMaterial(teacher, java.getId(), "Lecture 2 - Classes, objects and inheritance");
        addAssignment(teacher, java.getId(), "Hello OOP", "Create a Student class with 3 fields and a toString()", 10);
        enroll(student, java.getId());
        createCourse(teacher, "Data Structures", "Arrays, linked lists, stacks, queues and trees");
    }
}
