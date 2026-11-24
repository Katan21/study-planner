package com.studyplanner.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class StudyBlockDto {
    private Long id;
    private LocalDate date;
    private String subject;
    private String activity;
    private String status;
    private LocalDate examDate;
    private LocalTime examTime;
    private LocalDate postponedTo;

    public StudyBlockDto() {}

    public StudyBlockDto(LocalDate date, String subject, String activity) {
        this.date = date;
        this.subject = subject;
        this.activity = activity;
    }

    public StudyBlockDto(Long id, LocalDate date, String subject,
                         String activity, String status, LocalDate examDate,
                         LocalTime examTime, LocalDate postponedTo) {
        this.id = id;
        this.date = date;
        this.subject = subject;
        this.activity = activity;
        this.status = status;
        this.examDate = examDate;
        this.examTime = examTime;
        this.postponedTo = postponedTo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public StudyBlockDto(Long id, LocalDate date, String subject,
                     String activity, String status, LocalDate examDate) {
    this(id, date, subject, activity, status, examDate, null, null);
}
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public LocalTime getExamTime() { return examTime; }
    public void setExamTime(LocalTime examTime) { this.examTime = examTime; }

    public LocalDate getPostponedTo() { return postponedTo; }
    public void setPostponedTo(LocalDate postponedTo) { this.postponedTo = postponedTo; }
}
