package com.studyplanner.dto;

import java.time.LocalDate;

public class StudyBlockDto {
    private LocalDate date;
    private String subject;
    private String activity; // например, "study" или "final revision"

    public StudyBlockDto() {}

    public StudyBlockDto(LocalDate date, String subject, String activity) {
        this.date = date;
        this.subject = subject;
        this.activity = activity;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }
}