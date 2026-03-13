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

    List<Employee> findByDirectManagerId(Integer directManagerId);

    // Tìm theo userId
    Optional<Employee> findByUserId(Integer userId);

    List<Employee> findByDeptId(Integer deptId);

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
        ORDER BY e.empId ASC
    """)
    List<Employee> findAllEmployeesOnly();

    @Query("""
    select e from Employee e
    where (e.status is null or e.status not in ('RESIGNED','TERMINATED'))
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
    order by e.fullName asc
""")
    List<Employee> searchAvailableForBatch(@Param("batchId") Integer batchId,
                                           @Param("kw") String kw,
                                           Pageable pageable);
}