package com.example.hrm.service;

import com.example.hrm.entity.ShiftTemplate;

import java.util.List;

public interface ShiftTemplateService {
    List<ShiftTemplate> findAll();
    ShiftTemplate findById(Integer id);
    ShiftTemplate create(ShiftTemplate shift);
    ShiftTemplate update(Integer id, ShiftTemplate req);
    void deactivate(Integer id);
    void delete(Integer id);
}