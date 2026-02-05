package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

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
    private Integer id;

    @Column(name = "user_id", unique = true)
    private Integer userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "direct_manager_id")
    private Integer directManagerId;

    @Column(name = "status")
    private String status; // PROBATION/OFFICIAL/RESIGNED/TERMINATED
}
