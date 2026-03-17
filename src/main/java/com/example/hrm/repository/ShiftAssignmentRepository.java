package com.example.hrm.repository;

import com.example.hrm.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByBatch_BatchIdOrderByWorkDateAscEmployee_EmpIdAsc(Long batchId);
    Optional<ShiftAssignment> findByEmployee_EmpIdAndWorkDate(Integer empId, LocalDate workDate);
    Optional<ShiftAssignment> findTopByEmployee_EmpIdAndWorkDateLessThanEqualOrderByWorkDateDesc(Integer empId, LocalDate workDate);
    List<ShiftAssignment> findByWorkDateAndAssignmentType(LocalDate workDate, String assignmentType);
}