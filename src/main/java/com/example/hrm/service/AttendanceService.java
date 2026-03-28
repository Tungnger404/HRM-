package com.example.hrm.service;

import com.example.hrm.entity.AttendanceLog;

import java.util.List;

public interface AttendanceService {
    void checkIn(Integer empId);
    void checkOut(Integer empId);
    Integer getEmpIdFromSecurity();
    List<AttendanceLog> getHistory(Integer empId);
    Integer getEmpIdByUsername(String username);
}