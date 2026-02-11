package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {



    // Search theo tên
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);

    // Tìm theo userId
    Optional<Employee> findByUserId(Integer userId);

    // Count theo status (Active/On Leave/Resigned...)
    long countByStatusIgnoreCase(String status);

    // Count theo ngày join sau mốc
    long countByJoinDateAfter(LocalDate date);

    // HR search: name + status
    @Query("""
        SELECT e FROM Employee e
        WHERE (:q IS NULL OR :q = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:status IS NULL OR :status = '' OR e.status = :status)
        ORDER BY e.empId DESC
    """)
    List<Employee> search(@Param("q") String q,
                          @Param("status") String status);
}
