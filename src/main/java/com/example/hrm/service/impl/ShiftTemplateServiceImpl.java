package com.example.hrm.service.impl;

import com.example.hrm.entity.ShiftTemplate;
import com.example.hrm.repository.ShiftTemplateRepository;
import com.example.hrm.service.ShiftTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftTemplateServiceImpl implements ShiftTemplateService {

    private final ShiftTemplateRepository repository;

    public ShiftTemplateServiceImpl(ShiftTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ShiftTemplate> findAll() {
        return repository.findAll();
    }

    @Override
    public ShiftTemplate findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + id));
    }

    @Override
    public ShiftTemplate create(ShiftTemplate shift) {
        if (repository.findByShiftCode(shift.getShiftCode()).isPresent()) {
            throw new RuntimeException("Shift code already exists: " + shift.getShiftCode());
        }
        if (shift.getIsActive() == null) shift.setIsActive(true);
        if (shift.getIsOvernight() == null) shift.setIsOvernight(false);
        return repository.save(shift);
    }

    @Override
    public ShiftTemplate update(Integer id, ShiftTemplate req) {
        ShiftTemplate cur = findById(id);
        cur.setShiftName(req.getShiftName());
        cur.setStartTime(req.getStartTime());
        cur.setEndTime(req.getEndTime());
        cur.setIsOvernight(req.getIsOvernight());
        cur.setIsActive(req.getIsActive());
        return repository.save(cur);
    }

    @Override
    public void deactivate(Integer id) {
        ShiftTemplate cur = findById(id);
        cur.setIsActive(false);
        repository.save(cur);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}