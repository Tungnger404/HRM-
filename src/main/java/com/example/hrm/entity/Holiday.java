package com.example.hrm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @org.hibernate.annotations.Nationalized
    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String title;

    @Column(nullable = false, name = "holiday_date")
    private LocalDate holidayDate;

    @org.hibernate.annotations.Nationalized
    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    @Column(nullable = false)
    private String status; // "ACTIVE" or "INACTIVE"

    public Holiday() {
    }

    public Holiday(String title, LocalDate holidayDate, String description, String status) {
        this.title = title;
        this.holidayDate = holidayDate;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
