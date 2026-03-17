package com.example.hrm.repository;

import com.example.hrm.entity.Employee;
import com.example.hrm.entity.Payslip;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PayslipRepository extends JpaRepository<Payslip, Integer> {

    List<Payslip> findByEmployeeOrderByIdDesc(Employee employee);

    List<Payslip> findByBatch_IdOrderByIdAsc(Integer batchId);
    boolean existsByBatch_IdAndEmployee_Id(Integer batchId, Integer empId);

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
            s.totalIncome,
            s.totalDeduction,
            s.netSalary,
            b.status,
            b.name,
            s.baseSalary,
            s.standardWorkDays,
            s.actualWorkDays,
            s.otHours,
            jp.title,
            s.sentToEmployee,
            s.slipStatus,
            s.rejectReason,
            s.rejectedAt
            from Payslip s
            join s.batch b
            join b.period per
            join s.employee e
            left join JobPosition jp on jp.jobId = e.jobId
            where (:managerEmpId is null or e.directManagerId = :managerEmpId)
              and (:periodId is null or per.id = :periodId)
              and (
                    :status is null or :status = ''
                    or (:status = 'REJECTED'
                        and upper(coalesce(s.slipStatus, 'ACTIVE')) = 'REJECTED')

                    or (:status = 'DRAFT'
                        and upper(coalesce(b.status, '')) = 'DRAFT'
                        and upper(coalesce(s.slipStatus, 'ACTIVE')) not in ('REJECTED', 'APPROVED'))

                    or (:status = 'PENDING_APPROVAL'
                        and upper(coalesce(b.status, '')) = 'PENDING_APPROVAL'
                        and upper(coalesce(s.slipStatus, 'ACTIVE')) not in ('REJECTED', 'APPROVED'))

                    or (:status = 'APPROVED'
                        and (upper(coalesce(s.slipStatus, 'ACTIVE')) = 'APPROVED'
                             or (upper(coalesce(b.status, '')) in ('APPROVED', 'PAID')
                                 and upper(coalesce(s.slipStatus, 'ACTIVE')) <> 'REJECTED')))

                    or (:status = 'PAID'
                        and upper(coalesce(b.status, '')) = 'PAID'
                        and upper(coalesce(s.slipStatus, 'ACTIVE')) <> 'REJECTED')
              )
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
                                      @Param("empId") Integer empId,
                                      @Param("periodId") Integer periodId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from Payslip p
                where p.batch.id = :batchId
            """)
    int deleteByBatchId(@Param("batchId") Integer batchId);

    @Query("select s.batch.id from Payslip s where s.id in :payslipIds")
    List<Integer> findBatchIdsByPayslipIds(@Param("payslipIds") List<Integer> payslipIds);

    long countByBatch_Id(Integer batchId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PayslipItem it where it.payslip.id = :payslipId")
    int deleteByPayslipId(@Param("payslipId") Integer payslipId);

    @Query("""
    select p from Payslip p
    join fetch p.batch b
    join fetch b.period per
    join fetch p.employee e
    where e.empId = :empId
      and upper(coalesce(p.slipStatus, 'ACTIVE')) <> 'REJECTED'
      and upper(coalesce(b.status, '')) in ('APPROVED', 'PAID')
    order by per.year desc, per.month desc, p.id desc
""")
    List<Payslip> findReleasedByEmployeeId(@Param("empId") Integer empId);
}


