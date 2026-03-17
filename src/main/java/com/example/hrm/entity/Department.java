package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "departments")
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Integer deptId;
    @Column(name = "manager_id")
    private Integer managerId;

    @Column(name = "dept_name")
    private String deptName;

}
