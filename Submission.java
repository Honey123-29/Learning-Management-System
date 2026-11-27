package com.lms.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String studentId;
    private final String content;
    private final LocalDateTime submittedAt;
    private Integer marks;      // null = not graded yet
    private String feedback;

    public Submission(String studentId, String content) {
        this.studentId = studentId;
        this.content = content;
        this.submittedAt = LocalDateTime.now();
    }

    public String getStudentId() { return studentId; }
    public String getContent() { return content; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public Integer getMarks() { return marks; }
    public String getFeedback() { return feedback; }
    public boolean isGraded() { return marks != null; }

    public void grade(int marks, String feedback) {
        this.marks = marks;
        this.feedback = feedback;
    }
}
