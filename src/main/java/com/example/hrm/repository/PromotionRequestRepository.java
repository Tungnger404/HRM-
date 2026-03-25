package com.example.hrm.repository;

import com.example.hrm.entity.PromotionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestRepository extends JpaRepository<PromotionRequest, Integer> {
    
    List<PromotionRequest> findByStatusOrderByRequestedAtDesc(PromotionRequest.RequestStatus status);
    
    List<PromotionRequest> findByRequestedByOrderByRequestedAtDesc(Integer requestedBy);
    
    List<PromotionRequest> findByEmpIdOrderByRequestedAtDesc(Integer empId);

    boolean existsByEmpIdAndStatus(Integer empId, PromotionRequest.RequestStatus status);
}
