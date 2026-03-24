package com.example.hrm.controller;

import com.example.hrm.entity.*;
import com.example.hrm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
public class EmployeeTimesheetController {

    @Autowired private UserAccountRepository accountRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AttendanceLogRepository logRepo;
    @Autowired private ShiftAssignmentRepository shiftRepo;
    @Autowired private LeaveOrOtRequestRepository leaveRepo;
    @Autowired private HolidayRepository holidayRepo;

    @GetMapping("/employee/timesheets")
    public String viewTimesheets(@RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year,
                                 Authentication authentication, Model model) {
        if (authentication == null) return "redirect:/login";
        String username = authentication.getName();
        UserAccount account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) return "redirect:/login";
        
        Employee employee = employeeRepository.findByUserId(account.getId()).orElse(null);
        if (employee == null) return "redirect:/login";

        Integer empId = employee.getEmpId();

        int y = (year != null) ? year : LocalDate.now().getYear();
        int m = (month != null) ? month : LocalDate.now().getMonthValue();
        YearMonth ym = YearMonth.of(y, m);

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // Fetch data
        List<AttendanceLog> logs = logRepo.findByEmployee_EmpIdAndWorkDateBetweenOrderByWorkDateAsc(empId, start, end);
        List<ShiftAssignment> shifts = shiftRepo.findByEmployee_EmpIdAndWorkDateBetween(empId, start, end);
        List<LeaveOrOtRequest> leaves = leaveRepo.findByEmpIdAndRequestTypeAndStatus(empId, RequestType.LEAVE, RequestStatus.APPROVED);
        List<Holiday> holidays = holidayRepo.findAll().stream()
                .filter(h -> h.getHolidayDate() != null && !h.getHolidayDate().isBefore(start) && !h.getHolidayDate().isAfter(end))
                .filter(h -> "ACTIVE".equalsIgnoreCase(h.getStatus()))
                .toList();

        int totalWorked = 0;
        int totalAbsent = 0;
        int totalPaidLeave = 0;
        int totalHoliday = 0;

        List<List<Map<String, Object>>> weeks = new ArrayList<>();
        List<Map<String, Object>> currentWeek = new ArrayList<>();

        // Add padding for days before the 1st of the month (Monday = 1 ... Sunday = 7)
        int firstDayOfWeek = start.getDayOfWeek().getValue();
        for (int i = 1; i < firstDayOfWeek; i++) {
            Map<String, Object> emptyDay = new HashMap<>(); // Empty day
            emptyDay.put("isEmpty", true);
            emptyDay.put("isToday", false);
            emptyDay.put("status", "PADDING");
            currentWeek.add(emptyDay);
        }

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("isEmpty", false);
            dayInfo.put("date", date);
            dayInfo.put("dayOfWeek", date.getDayOfWeek().name());
            dayInfo.put("dayNumber", day);
            dayInfo.put("isToday", date.isEqual(LocalDate.now()));

            boolean isFuture = date.isAfter(LocalDate.now());

            boolean hasLog = logs.stream().anyMatch(l -> l.getWorkDate().equals(date) && l.getCheckIn() != null);
            boolean isHoliday = holidays.stream().anyMatch(h -> h.getHolidayDate().equals(date));
            
            boolean isLeave = leaves.stream().anyMatch(l -> 
                (l.getTargetWorkDate() != null && l.getTargetWorkDate().equals(date)) ||
                (l.getStartTime() != null && l.getEndTime() != null && 
                 !date.isBefore(l.getStartTime().toLocalDate()) && !date.isAfter(l.getEndTime().toLocalDate()))
            );

            ShiftAssignment shift = shifts.stream().filter(s -> s.getWorkDate().equals(date)).findFirst().orElse(null);
            boolean isAssignedWork = (shift != null && "WORK".equals(shift.getAssignmentType()));
            if (shift == null) {
                int dow = date.getDayOfWeek().getValue();
                isAssignedWork = (dow >= 1 && dow <= 5);
            }

            String status = "";
            String badgeClass = "";
            String dotColor = "#cbd5e1"; // default gray

            if (isFuture) {
                if (isHoliday) { 
                    status = "HOLIDAY"; badgeClass = "bg-secondary"; dotColor = "#6c757d";
                    totalHoliday++;
                }
                else if (isLeave) { status = "PAID LEAVE"; badgeClass = "bg-info text-dark"; dotColor = "#0dcaf0"; }
                else if (isAssignedWork) { status = "SCHEDULED"; badgeClass = "bg-light text-dark border"; dotColor = "#dee2e6"; }
                else { status = "DAY OFF"; badgeClass = "bg-light text-muted border"; dotColor = "#e2e8f0"; }
            } else {
                if (hasLog) {
                    status = "WORKED"; badgeClass = "bg-success"; dotColor = "#198754";
                    totalWorked++;
                } else if (isLeave) {
                    status = "PAID LEAVE"; badgeClass = "bg-info text-dark"; dotColor = "#0dcaf0";
                    totalPaidLeave++;
                } else if (isHoliday) {
                    status = "HOLIDAY"; badgeClass = "bg-secondary"; dotColor = "#6c757d";
                    totalHoliday++;
                } else if (isAssignedWork) {
                    status = "ABSENT"; badgeClass = "bg-danger"; dotColor = "#dc3545";
                    totalAbsent++;
                } else {
                    status = "DAY OFF"; badgeClass = "bg-light text-muted border"; dotColor = "#e2e8f0";
                }
            }

            if (hasLog) {
                AttendanceLog log = logs.stream().filter(l -> l.getWorkDate().equals(date) && l.getCheckIn() != null).findFirst().orElse(null);
                if (log != null && log.getCheckOut() != null) {
                    String inStr = log.getCheckIn().toLocalTime().toString().substring(0, 5);
                    String outStr = log.getCheckOut().toLocalTime().toString().substring(0, 5);
                    dayInfo.put("timeRaw", inStr + " - " + outStr);
                } else {
                    dayInfo.put("timeRaw", "Missing Out");
                    status = "MISSING LOG"; badgeClass = "bg-warning text-dark"; dotColor = "#ffc107";
                }
            } else {
                dayInfo.put("timeRaw", "");
            }

            dayInfo.put("status", status);
            dayInfo.put("badgeClass", badgeClass);
            dayInfo.put("dotColor", dotColor);
            
            currentWeek.add(dayInfo);

            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }
        }

        // Add padding for days after the end of the month
        if (!currentWeek.isEmpty()) {
            while (currentWeek.size() < 7) {
                Map<String, Object> emptyDay = new HashMap<>();
                emptyDay.put("isEmpty", true);
                emptyDay.put("isToday", false);
                emptyDay.put("status", "PADDING");
                currentWeek.add(emptyDay);
            }
            weeks.add(currentWeek);
        }

        model.addAttribute("currentYear", y);
        model.addAttribute("currentMonth", m);
        model.addAttribute("weeks", weeks);
        model.addAttribute("totalWorked", totalWorked);
        model.addAttribute("totalAbsent", totalAbsent);
        model.addAttribute("totalPaidLeave", totalPaidLeave);
        model.addAttribute("totalHoliday", totalHoliday);
        model.addAttribute("employee", employee);

        // Month pagination aids
        YearMonth prevMonth = ym.minusMonths(1);
        YearMonth nextMonth = ym.plusMonths(1);
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("monthName", ym.getMonth().name());

        return "auth/employee-timesheets";
    }
}
