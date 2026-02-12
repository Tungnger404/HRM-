package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.Payslip;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, Integer> {

    List<Payslip> findByEmployeeOrderByIdDesc(Employee employee);

    List<Payslip> findByBatch_IdOrderByIdAsc(Integer batchId);

    @Query("""
                select
                    s.id,
                    b.id,
                    e.empId,
                    e.fullName,
                    per.month,
                    per.year,
                    per.startDate,
                    per.endDate,
                    s.netSalary,
                    b.status,
                    s.baseSalary,
                    s.standardWorkDays,
                    jp.title
                from Payslip s
                join s.batch b
                join b.period per
                join s.employee e
                left join JobPosition jp on jp.jobId = e.jobId
                where (:managerEmpId is null or e.directManagerId = :managerEmpId)
                  and (:status is null or :status = '' or b.status = :status)
                  and (
                        :kw is null or :kw = ''
                        or lower(e.fullName) like lower(concat('%', :kw, '%'))
                        or (:empId is not null and e.empId = :empId)
                  )
                order by per.year desc, per.month desc, e.empId asc, s.id desc
            """)
    List<Object[]> findPayrollRowsRaw(@Param("managerEmpId") Integer managerEmpId,
                                      @Param("status") String status,
                                      @Param("kw") String kw,
                                      @Param("empId") Integer empId);

}
