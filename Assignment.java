package com.lms.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String description;
    private final int maxMarks;
    private final Map<String, Submission> submissions = new LinkedHashMap<>();

    public Assignment(String id, String title, String description, int maxMarks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.maxMarks = maxMarks;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getMaxMarks() { return maxMarks; }

    public Submission getSubmission(String studentId) { return submissions.get(studentId); }
    public Collection<Submission> getSubmissions() { return submissions.values(); }
    public void putSubmission(Submission s) { submissions.put(s.getStudentId(), s); }

    @Override
    public String toString() {
        return String.format("%-7s %-28s max marks: %d", id, title, maxMarks);
    }
}
