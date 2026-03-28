package com.example.hrm.controller;
import com.example.hrm.service.AttendanceAutoService;
import org.springframework.security.core.Authentication;
import com.example.hrm.service.AttendanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employee/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceAutoService attendanceAutoService;
    private final com.example.hrm.repository.EmployeeRepository employeeRepository;
    private final com.example.hrm.repository.UserRepository userRepository;
    private final com.example.hrm.repository.HolidayRepository holidayRepository;

    public AttendanceController(AttendanceService attendanceService,
                                AttendanceAutoService attendanceAutoService,
                                com.example.hrm.repository.EmployeeRepository employeeRepository,
                                com.example.hrm.repository.UserRepository userRepository,
                                com.example.hrm.repository.HolidayRepository holidayRepository) {
        this.attendanceService = attendanceService;
        this.attendanceAutoService = attendanceAutoService;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.holidayRepository = holidayRepository;
    }
    @GetMapping
    public String attendancePage(Model model, Authentication auth) {

        Integer empId = attendanceService.getEmpIdFromSecurity();
        java.util.List<com.example.hrm.entity.AttendanceLog> logs = attendanceService.getHistory(empId);
        model.addAttribute("logs", logs);

        if (auth != null) {
            String username = auth.getName();
            com.example.hrm.entity.User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                com.example.hrm.entity.Employee emp = employeeRepository.findByUserId(user.getUserId()).orElse(null);
                model.addAttribute("u", user);
                model.addAttribute("e", emp);
            }
        }

        long hoursToday = 0;
        long hoursWeek = 0;
        long hoursMonth = 0;
        long overtimeMonth = 0;
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());

        for (com.example.hrm.entity.AttendanceLog log : logs) {
            if (log.getCheckIn() != null && log.getCheckOut() != null) {
                long dur = java.time.Duration.between(log.getCheckIn(), log.getCheckOut()).toMinutes();
                long ot = 0;
                if (log.getScheduledEndAt() != null && log.getCheckOut().isAfter(log.getScheduledEndAt())) {
                    ot = java.time.Duration.between(log.getScheduledEndAt(), log.getCheckOut()).toMinutes();
                }
                
                if (log.getWorkDate().isEqual(today)) {
                    hoursToday += dur;
                }
                if (log.getWorkDate().get(weekFields.weekOfWeekBasedYear()) == currentWeek && log.getWorkDate().getYear() == today.getYear()) {
                    hoursWeek += dur;
                }
                if (log.getWorkDate().getMonth() == today.getMonth() && log.getWorkDate().getYear() == today.getYear()) {
                    hoursMonth += dur;
                    overtimeMonth += ot;
                }
            }
        }
        
        model.addAttribute("hrsToday", String.format("%.2f", hoursToday / 60.0));
        model.addAttribute("hrsWeek", String.format("%.2f", hoursWeek / 60.0));
        model.addAttribute("hrsMonth", String.format("%.2f", hoursMonth / 60.0));
        model.addAttribute("otMonth", String.format("%.2f", overtimeMonth / 60.0));

        boolean checkedInToday = logs.stream()
                .anyMatch(log -> log.getWorkDate() != null && log.getWorkDate().isEqual(today)
                        && log.getCheckIn() != null);
        model.addAttribute("checkedInToday", checkedInToday);

        return "employee/attendance";
    }

    @PostMapping("/check-in")
    public String checkIn(RedirectAttributes ra, jakarta.servlet.http.HttpServletRequest request) {

        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            attendanceService.checkIn(empId);
            ra.addFlashAttribute("message", "Check-in successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/employee/attendance");
    }

    @PostMapping("/check-out")
    public String checkOut( RedirectAttributes ra, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Integer empId = attendanceService.getEmpIdFromSecurity();
            attendanceService.checkOut(empId);
            ra.addFlashAttribute("message", "Check-out successful!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/employee/attendance");

    }

    @GetMapping("/timesheet")
    public String timesheetPage(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
                                @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
                                Model model, Authentication auth) {
        Integer empId = attendanceService.getEmpIdFromSecurity();
        java.time.LocalDate today = java.time.LocalDate.now();
        int targetYear = (year != null) ? year : today.getYear();
        int targetMonth = (month != null) ? month : today.getMonthValue();
        
        java.time.YearMonth ym = java.time.YearMonth.of(targetYear, targetMonth);

        java.util.List<com.example.hrm.entity.AttendanceLog> allLogs = attendanceService.getHistory(empId);
        
        java.util.List<com.example.hrm.entity.AttendanceLog> monthLogs = allLogs.stream()
            .filter(log -> log.getWorkDate() != null && log.getWorkDate().getYear() == targetYear && log.getWorkDate().getMonthValue() == targetMonth)
            .collect(java.util.stream.Collectors.toList());

        int standardDays = 0;
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            java.time.DayOfWeek dow = ym.atDay(i).getDayOfWeek();
            if (dow != java.time.DayOfWeek.SATURDAY && dow != java.time.DayOfWeek.SUNDAY) {
                standardDays++;
            }
        }
        

        int lateArrivalsMins = monthLogs.stream()
            .filter(log -> Boolean.TRUE.equals(log.getIsLate()) && log.getLateMinutes() != null)
            .mapToInt(com.example.hrm.entity.AttendanceLog::getLateMinutes)
            .sum();
        
        int paidLeave = (int) monthLogs.stream()
            .filter(log -> "LEAVE".equals(log.getStatus()) || "APPROVED_LEAVE".equals(log.getStatus()))
            .count();
        
        int unpaidLeave = (int) monthLogs.stream()
            .filter(log -> "ABSENT".equals(log.getStatus()))
            .count();
        
        int actualWorkDays = (int) monthLogs.stream()
            .filter(log -> log.getCheckIn() != null)
            .count();

        java.util.List<java.util.Map<String, Object>> logList = new java.util.ArrayList<>();
        for (com.example.hrm.entity.AttendanceLog log : monthLogs) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("date", log.getWorkDate().toString());
            map.put("status", log.getStatus());
            map.put("checkIn", log.getCheckIn() != null ? log.getCheckIn().toLocalTime().toString() : null);
            map.put("checkOut", log.getCheckOut() != null ? log.getCheckOut().toLocalTime().toString() : null);
            map.put("shiftId", log.getShiftId());
            map.put("isLate", log.getIsLate());
            map.put("lateMinutes", log.getLateMinutes());
            map.put("scheduledStartAt", log.getScheduledStartAt() != null ? log.getScheduledStartAt().toLocalTime().toString() : null);
            map.put("scheduledEndAt", log.getScheduledEndAt() != null ? log.getScheduledEndAt().toLocalTime().toString() : null);
            logList.add(map);
        }

        java.util.List<com.example.hrm.entity.Holiday> activeHolidays = holidayRepository.findAll().stream()
                .filter(h -> "ACTIVE".equals(h.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        java.util.List<java.util.Map<String, Object>> holidayList = new java.util.ArrayList<>();
        for (com.example.hrm.entity.Holiday h : activeHolidays) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("title", h.getTitle());
            map.put("start", h.getHolidayDate().toString());
            map.put("end", h.getEndDate() != null ? h.getEndDate().toString() : h.getHolidayDate().toString());
            holidayList.add(map);
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            model.addAttribute("logsJson", mapper.writeValueAsString(logList));
            model.addAttribute("holidaysJson", mapper.writeValueAsString(holidayList));
        } catch (Exception e) {}

        model.addAttribute("currentYear", targetYear);
        model.addAttribute("currentMonth", targetMonth);
        model.addAttribute("standardDays", standardDays);
        model.addAttribute("actualWorkDays", actualWorkDays);
        model.addAttribute("lateArrivalsMins", lateArrivalsMins);
        model.addAttribute("paidLeave", paidLeave);
        model.addAttribute("unpaidLeave", unpaidLeave);

        return "employee/timesheet";
    }

    @PostMapping("/dev/mark-absent-now")
    public String markAbsentNow(RedirectAttributes ra) {
        try {
            attendanceAutoService.markAbsentForToday();
            ra.addFlashAttribute("message", "Marked absent job executed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/attendance";
    }
}
