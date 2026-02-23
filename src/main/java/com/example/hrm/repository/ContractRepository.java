package com.example.hrm.repository;

import com.example.hrm.entity.Contract;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    @Query("""
        select c from Contract c
        where c.employee.empId = :empId
          and c.status = 'ACTIVE'
          and c.startDate <= :periodEnd
          and (c.endDate is null or c.endDate >= :periodStart)
        order by c.startDate desc
    """)
    Optional<Contract> findActiveContractForPeriod(@Param("empId") Integer empId,
                                                   @Param("periodStart") LocalDate periodStart,
                                                   @Param("periodEnd") LocalDate periodEnd);


    // List hợp đồng theo nhân viên (phục vụ HR xem lịch sử contract)
    List<Contract> findByEmployee_EmpIdOrderByStartDateDesc(Integer empId);

    // List tất cả (mới nhất trước) để HR xem tổng quan
    @Query("select c from Contract c order by c.id desc")
    List<Contract> findAllNewest();

    // (Optional) Nếu muốn lọc theo status, tiện cho màn HR
    List<Contract> findByStatusOrderByStartDateDesc(String status);

    // (Optional) Nếu muốn tìm hợp đồng ACTIVE hiện tại của emp (ngoài payroll period)
    @Query("""
        select c from Contract c
        where c.employee.empId = :empId
          and c.status = 'ACTIVE'
        order by c.startDate desc
    """)
    List<Contract> findActiveContracts(@Param("empId") Integer empId);
}
