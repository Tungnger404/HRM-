package com.example.hrm.service;

import com.example.hrm.dto.BatchInterviewDTO;
import com.example.hrm.dto.CandidateEvaluateDTO;
import com.example.hrm.dto.CandidateListDTO;
import com.example.hrm.dto.ApplyFormDTO;
import com.example.hrm.entity.Candidate;
import com.example.hrm.entity.CandidateStatus;

import java.util.List;

public interface CandidateService {

    // ===== HR SCREENING =====
    List<CandidateListDTO> getCandidates(Integer postingId,
                                         CandidateStatus status,
                                         String keyword);

    CandidateEvaluateDTO getEvaluateDTO(Integer id);

    void evaluate(CandidateEvaluateDTO dto);

    Candidate findById(Integer id);


    void apply(String slug, ApplyFormDTO form);
    void scheduleBatchInterview(BatchInterviewDTO dto);

}