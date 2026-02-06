package com.example.hrm.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_id")
    private Integer empId;

    // employees.user_id UNIQUE, FK -> users(user_id)
    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // MALE/FEMALE/OTHER
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

    // self-FK -> employees.emp_id
    @Column(name = "direct_manager_id")
    private Integer directManagerId;

    // PROBATION/OFFICIAL/RESIGNED/TERMINATED
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "join_date")
    private LocalDate joinDate;

    // ===== getters/setters =====

    public Integer getEmpId() { return empId; }
    public void setEmpId(Integer empId) { this.empId = empId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdentityCard() { return identityCard; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public Integer getJobId() { return jobId; }
    public void setJobId(Integer jobId) { this.jobId = jobId; }

    public Integer getDirectManagerId() { return directManagerId; }
    public void setDirectManagerId(Integer directManagerId) { this.directManagerId = directManagerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
}
