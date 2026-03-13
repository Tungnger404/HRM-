package com.example.hrm.service;
import com.example.hrm.entity.AttendanceLog;
import com.example.hrm.entity.Employee;
import com.example.hrm.repository.AttendanceLogRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.hrm.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;


    public AttendanceService(AttendanceLogRepository attendanceLogRepository,EmployeeRepository employeeRepository,UserRepository userRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository=userRepository;

    }

    // ================= CHECK IN =================
    public void checkIn(Integer empId) {

        LocalDate today = LocalDate.now();

        // 🔥 Kiểm tra hôm nay đã check-in chưa (dùng query có sẵn)
        if (attendanceLogRepository
                .findByEmployee_EmpIdAndWorkDate(empId, today)
                .isPresent()) {

            throw new RuntimeException("You have already checked in today.");
        }

        // 🔥 Lấy Employee từ DB
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();

        AttendanceLog log = AttendanceLog.builder()
                .employee(employee)
                .workDate(today)
                .checkIn(now)
                .status(now.getHour() >= 8 ? "LATE" : "ON_TIME")
                .workType("NOMAL")
                .build();

        attendanceLogRepository.save(log);
    }

    // ================= CHECK OUT =================
    public void checkOut(Integer empId) {

        LocalDate today = LocalDate.now();

        AttendanceLog log = attendanceLogRepository
                .findByEmployee_EmpIdAndWorkDate(empId, today)
                .orElseThrow(() -> new RuntimeException("You have not checked in today."));
        if ("ABSENT".equals(log.getStatus())) {
            throw new RuntimeException("You are marked absent today, cannot check out.");
        }

        if (log.getCheckOut() != null) {
            throw new RuntimeException("You already checked out.");
        }

        log.setCheckOut(LocalDateTime.now());

        attendanceLogRepository.save(log);
    }

    public Integer getEmpIdFromSecurity() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }
    // ================= HISTORY =================
    public List<AttendanceLog> getLogsByEmp(Integer empId) {
        return attendanceLogRepository.findByEmployee_EmpId(empId);
    }

    public List<AttendanceLog> getHistory(Integer empId) {
        return attendanceLogRepository.findByEmployee_EmpId(empId);
    }
    public Integer getEmpIdByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee emp = employeeRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return emp.getEmpId();
    }

}