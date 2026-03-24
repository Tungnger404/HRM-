package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.repository.view.EmployeeStatusCountView;
import com.example.hrm.repository.view.JobEmployeeCountView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    List<Employee> findByFullNameContainingIgnoreCase(String keyword);

    List<Employee> findByDirectManagerId(Integer directManagerId);

    Optional<Employee> findByUserId(Integer userId);

    List<Employee> findByDeptId(Integer deptId);

    long countByStatusIgnoreCase(String status);

    long countByJoinDateAfter(LocalDate date);

    @Query("""
                SELECT e FROM Employee e
                WHERE (:q IS NULL OR :q = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
                  AND (:status IS NULL OR :status = '' OR e.status = :status)
                ORDER BY e.empId DESC
            """)
    List<Employee> search(@Param("q") String q,
                          @Param("status") String status);

    long countByStatus(String status);

    List<Employee> findByJobId(Integer jobId);

    @Query("""
                select e.jobId as jobId, count(e) as cnt
                from Employee e
                where e.jobId in :jobIds
                group by e.jobId
            """)
    List<JobEmployeeCountView> countByJobIds(@Param("jobIds") List<Integer> jobIds);

    @Query("""
        SELECT e FROM Employee e
        JOIN UserAccount u ON e.userId = u.id
        JOIN Role r ON u.role.id = r.id
        WHERE r.roleName = 'HR'
        ORDER BY e.empId ASC
    """)
    List<Employee> findHrStaff();

    @Query("""
        SELECT e FROM Employee e
        JOIN UserAccount u ON e.userId = u.id
        JOIN Role r ON u.role.id = r.id
        WHERE r.roleName = 'EMPLOYEE'
          AND e.deptId = :deptId
        ORDER BY e.empId ASC
    """)
    List<Employee> findEmployeesOnlyByDeptId(@Param("deptId") Integer deptId);

    @Query("""
        SELECT e FROM Employee e
        JOIN UserAccount u ON e.userId = u.id
        JOIN Role r ON u.role.id = r.id
        WHERE r.roleName = 'EMPLOYEE'
        ORDER BY e.empId ASC
    """)
    List<Employee> findAllEmployeesOnly();

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

    @Query("""
        SELECT e FROM Employee e
        WHERE e.deptId IN (
            SELECT d.deptId FROM Department d WHERE d.managerId = :managerEmpId
        )
          AND (:q IS NULL OR :q = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:status IS NULL OR :status = '' OR e.status = :status)
        ORDER BY e.empId DESC
    """)
    List<Employee> searchEmployeesManagedByDepartment(@Param("managerEmpId") Integer managerEmpId,
                                                      @Param("q") String q,
                                                      @Param("status") String status);

    @Query("""
        SELECT COUNT(e) > 0 FROM Employee e
        WHERE e.empId = :empId
          AND e.deptId IN (
              SELECT d.deptId FROM Department d WHERE d.managerId = :managerEmpId
          )
    """)
    boolean existsManagedEmployee(@Param("managerEmpId") Integer managerEmpId,
                                  @Param("empId") Integer empId);

    @Query("""
    SELECT e FROM Employee e
    WHERE e.deptId = :managerDeptId
      AND (:q IS NULL OR :q = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:status IS NULL OR :status = '' OR e.status = :status)
    ORDER BY e.empId DESC
""")
    List<Employee> searchEmployeesBySameDepartment(@Param("managerDeptId") Integer managerDeptId,
                                                   @Param("q") String q,
                                                   @Param("status") String status);

    @Query("""
    SELECT COUNT(e) > 0 FROM Employee e
    WHERE e.empId = :empId
      AND e.deptId = :managerDeptId
""")
    boolean existsEmployeeInSameDepartment(@Param("managerDeptId") Integer managerDeptId,
                                           @Param("empId") Integer empId);

    List<Employee> findByDeptIdOrderByFullNameAsc(Integer deptId);

    @Query("""
        select upper(coalesce(e.status, 'UNKNOWN')) as status, count(e) as total
        from Employee e
        group by upper(coalesce(e.status, 'UNKNOWN'))
        order by upper(coalesce(e.status, 'UNKNOWN'))
    """)
    List<EmployeeStatusCountView> countEmployeesGroupByStatus();
}