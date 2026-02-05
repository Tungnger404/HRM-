package com.example.hrm.service;

import com.example.hrm.entity.LeaveOrOtRequest;
import com.example.hrm.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RequestService {
    @Autowired
    private RequestRepository requestRepository;

    public void submitRequest(LeaveOrOtRequest request) {
        request.setStatus("PENDING"); // Luôn để mặc định là chờ duyệt khi tạo mới
        requestRepository.save(request);
    }

    public void approveRequest(Integer requestId, Integer approverId) {
        LeaveOrOtRequest req = requestRepository.findById(requestId).orElse(null);
        if (req != null) {
            req.setStatus("APPROVED");
            req.setApproverId(approverId);
            requestRepository.save(req);
        }
    }
}