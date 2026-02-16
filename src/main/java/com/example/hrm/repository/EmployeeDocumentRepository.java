package com.example.hrm.repository;

import com.example.hrm.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Integer> {

    List<EmployeeDocument> findByEmployeeIdOrderByIdDesc(Integer employeeId);

    @Query("""
        select d from EmployeeDocument d
        where (:empId is null or d.employeeId = :empId)
          and (:docType is null or :docType = '' or lower(d.docType) = lower(:docType))
          and (:status is null or :status = '' or lower(d.status) = lower(:status))
          and (
                :q is null or :q = '' 
                or lower(d.title) like lower(concat('%', :q, '%'))
                or lower(d.fileName) like lower(concat('%', :q, '%'))
          )
        order by d.id desc
    """)
    List<EmployeeDocument> search(@Param("empId") Integer empId,
                                  @Param("docType") String docType,
                                  @Param("status") String status,
                                  @Param("q") String q);
}
