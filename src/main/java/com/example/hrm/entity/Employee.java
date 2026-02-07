package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Integer empId;

    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "identity_card", length = 20)
    private String identityCard;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "direct_manager_id")
    private Integer directManagerId;

    @Column(name = "status")
    private String status; // PROBATION/OFFICIAL/RESIGNED/TERMINATED

    // ✅ THÊM field này để EmployeeServiceImpl gọi được getJoinDate/setJoinDate
    @Column(name = "join_date")
    private LocalDate joinDate;

    // ✅ Compat: code cũ gọi getId()
    @Transient
    public Integer getId() {
        return this.empId;
    }

    public void setId(Integer id) {
        this.empId = id;
    }
}
