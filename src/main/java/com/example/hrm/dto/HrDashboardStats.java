package com.example.hrm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HrDashboardStats {
    private long totalEmployees;
    private long probationEmployees;
    private long officialEmployees;
    private long resignedEmployees;
    private long newHiresThisMonth;
    private long totalDepartments;
}
