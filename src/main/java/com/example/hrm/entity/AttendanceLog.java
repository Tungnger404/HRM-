package com.example.hrm.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name="attendance_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"emp_id", "work_date"}) // Đảm bảo mỗi nhân viên chỉ có 1 dòng/ngày
})
public class AttendanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "log_id")
    private Long logId; // BIGINT tương ứng với Long

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate; // DATE tương ứng với LocalDate

    @Column(name = "check_in")
    private LocalDateTime checkIn; // DATETIME tương ứng với LocalDateTime

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(length = 20)
    private String status; // NVARCHAR(20)

    public AttendanceLog() {
    }

    public AttendanceLog(long logId, int empId, LocalDate workDate, LocalDateTime checkIn, LocalDateTime checkOut, String status) {
        this.logId = logId;
        this.empId = empId;
        this.workDate = workDate;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
