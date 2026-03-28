package com.example.hrm.service;

import com.example.hrm.dto.MonthlyScheduleBatchFormDTO;
import com.example.hrm.entity.MonthlyScheduleBatch;

import java.util.List;

public interface MonthlyScheduleBatchService {
    List<MonthlyScheduleBatch> findAll();
    MonthlyScheduleBatch create(MonthlyScheduleBatchFormDTO form);
}