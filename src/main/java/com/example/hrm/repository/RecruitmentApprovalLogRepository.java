package com.example.hrm.repository;

import com.example.hrm.entity.RecruitmentApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentApprovalLogRepository
        extends JpaRepository<RecruitmentApprovalLog, Integer> {
}
