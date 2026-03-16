package com.example.hrm.repository;

import com.example.hrm.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    @Query("SELECT d.managerId FROM Department d WHERE d.deptId = :deptId")
    Integer findManagerIdByDeptId(@Param("deptId") Integer deptId);
}