package com.example.hrm.repository;

import com.example.hrm.entity.KpiAssignment;  // ✅ THÊM IMPORT NÀY
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiAssignmentRepository extends JpaRepository<KpiAssignment, Integer> {

    List<KpiAssignment> findByCycleId(Integer cycleId);

    List<KpiAssignment> findByEmpId(Integer empId);

    List<KpiAssignment> findByDeptId(Integer deptId);

    List<KpiAssignment> findByEmpIdAndCycleId(Integer empId, Integer cycleId);

    List<KpiAssignment> findByDeptIdAndCycleId(Integer deptId, Integer cycleId);

    List<KpiAssignment> findByKpiId(Integer kpiId);

    List<KpiAssignment> findByStatusIn(List<KpiAssignment.AssignmentStatus> statuses);

    List<KpiAssignment> findByStatusOrderByEmployeeSubmittedAtDesc(KpiAssignment.AssignmentStatus status);

    List<KpiAssignment> findByEmpIdOrderByAssignedAtDesc(Integer empId);

    List<KpiAssignment> findByEmpIdAndStatus(Integer empId, KpiAssignment.AssignmentStatus status);

    List<KpiAssignment> findByStatusOrderByManagerReviewedAtDesc(KpiAssignment.AssignmentStatus status);
}