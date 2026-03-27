package com.example.hrm.repository;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveOrOtRequestRepository extends JpaRepository<LeaveOrOtRequest, Integer> {

    @Query("""
        select r from LeaveOrOtRequest r
        where r.status = :status
          and exists (
            select 1 from Employee e
            where e.empId = r.empId
              and e.directManagerId = :managerEmpId
          )
        order by r.createdAt desc
    """)
    List<LeaveOrOtRequest> findManagerRequestsByStatus(@Param("managerEmpId") Integer managerEmpId,
                                                       @Param("status") RequestStatus status);

    List<LeaveOrOtRequest> findByStatusAndProcessedAtIsNullOrderByCreatedAtDesc(RequestStatus status);
    List<LeaveOrOtRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    List<LeaveOrOtRequest> findByStatusAndProcessedAtIsNotNullOrderByProcessedAtDesc(RequestStatus status);
    List<LeaveOrOtRequest> findByEmpIdOrderByCreatedAtDesc(Integer empId);
    List<LeaveOrOtRequest> findByStatusInOrderByManagerDecidedAtDesc(List<RequestStatus> statuses);

    List<LeaveOrOtRequest> findByEmpIdAndRequestTypeAndStatus(Integer empId, com.example.hrm.entity.RequestType requestType, com.example.hrm.entity.RequestStatus status);
}
