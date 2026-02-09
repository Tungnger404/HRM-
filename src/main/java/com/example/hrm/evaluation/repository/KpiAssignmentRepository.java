package com.example.hrm.evaluation.repository;

import com.example.hrm.evaluation.model.KpiAssignment;  // ✅ THÊM IMPORT NÀY
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
}