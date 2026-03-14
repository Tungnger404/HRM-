package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.repository.view.JobEmployeeCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

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

    // (Nếu bạn đang dùng thì giữ, nếu không dùng có thể xoá)
    long countByStatus(String status);

    // ✅ Lấy danh sách employees theo job_id
    List<Employee> findByJobId(Integer jobId);

    // ✅ Đếm số nhân viên theo job_id (chỉ cho các job trong page hiện tại)
    @Query("""
                select e.jobId as jobId, count(e) as cnt
                from Employee e
                where e.jobId in :jobIds
                group by e.jobId
            """)
    List<JobEmployeeCountView> countByJobIds(@Param("jobIds") List<Integer> jobIds);

    //search tên nhân viên + nhap ten tim
    @Query("""
        select e from Employee e
        where upper(coalesce(e.status, '')) in ('PROBATION', 'OFFICIAL')
          and coalesce(e.includeInPayroll, false) = false
          and not exists (select 1 from Payslip p2 where p2.employee.id = e.id)
          and (
               :managerEmpId is null
               or e.directManagerId = :managerEmpId
               or e.directManagerId is null
          )
          and (
               :kw is null or :kw = ''
               or lower(e.fullName) like lower(concat('%', :kw, '%'))
               or str(e.id) like concat('%', :kw, '%')
          )
          and not exists (
               select 1 from Payslip p
               where p.batch.id = :batchId
                 and p.employee.id = e.id
          )
        order by
            case when e.directManagerId = :managerEmpId then 0 else 1 end,
            e.fullName asc
        """)
    List<Employee> searchAvailableForBatch(@Param("batchId") Integer batchId,
                                           @Param("managerEmpId") Integer managerEmpId,
                                           @Param("kw") String kw,
                                           Pageable pageable);

    @Query("""
        select e
        from Employee e
        where (:managerId is null or e.directManagerId = :managerId)
          and coalesce(e.includeInPayroll, false) = false
          and not exists (select 1 from Payslip p where p.employee.id = e.id)
          and upper(coalesce(e.status, '')) in ('PROBATION', 'OFFICIAL')
        order by e.empId
        """)
    List<Employee> findEmployeesNotInPayroll(@Param("managerId") Integer managerId);

    @Query("""
        select e
        from Employee e
        where (:managerId is null or e.directManagerId = :managerId)
          and (coalesce(e.includeInPayroll, false) = true or exists (select 1 from Payslip p where p.employee.id = e.id))
          and (e.status is null or upper(e.status) not in ('RESIGNED', 'TERMINATED'))
        order by e.empId
        """)
    List<Employee> findPayrollEligibleEmployees(@Param("managerId") Integer managerId);

    @Query("""
            select e
            from Employee e
            where (:managerId is null or e.directManagerId = :managerId)
              and coalesce(e.includeInPayroll, false) = false
              and not exists (select 1 from Payslip p where p.employee.id = e.id)
              and (e.status is null or upper(e.status) not in ('RESIGNED', 'TERMINATED'))
            order by e.empId
            """)
    List<Employee> computefindEmployeesNotInPayroll(@Param("managerId") Integer managerId);
}