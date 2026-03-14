package com.example.hrm.service;

import com.example.hrm.entity.PromotionRequest;
import java.util.List;
import java.util.Map;

public interface PromotionService {
    
    List<Map<String, Object>> getEligibleEmployeesForPromotion(Integer requesterId);
    
    Map<String, Object> getEmployeeEvaluationHistory(Integer empId);
    
    PromotionRequest submitPromotionRequest(Integer empId, Integer proposedPositionId, 
                                           String reason, Integer requestedBy);
    
    List<PromotionRequest> getPendingPromotionRequests();
    
    PromotionRequest approvePromotionRequest(Integer requestId, Integer reviewerId, String comment);
    
    PromotionRequest rejectPromotionRequest(Integer requestId, Integer reviewerId, String comment);
    
    List<PromotionRequest> getMyPromotionRequests(Integer requestedBy);
}
