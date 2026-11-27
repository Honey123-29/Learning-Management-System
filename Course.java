package com.lms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String description;
    private final String instructorId;
    private final Set<String> studentIds = new LinkedHashSet<>();
    private final List<String> materials = new ArrayList<>();
    private final Map<String, Assignment> assignments = new LinkedHashMap<>();

    public Course(String id, String title, String description, String instructorId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructorId() { return instructorId; }

    public Set<String> getStudentIds() { return studentIds; }
    public List<String> getMaterials() { return materials; }
    public Collection<Assignment> getAssignments() { return assignments.values(); }
    public Assignment getAssignment(String assignmentId) { return assignments.get(assignmentId); }
    public void addAssignment(Assignment a) { assignments.put(a.getId(), a); }

    public boolean isEnrolled(String studentId) { return studentIds.contains(studentId); }
}
