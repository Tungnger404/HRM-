package com.example.hrm.service;

import com.example.hrm.entity.ShiftTemplate;
import com.example.hrm.repository.ShiftTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftTemplateService {
    private final ShiftTemplateRepository repository;

    public ShiftTemplateService(ShiftTemplateRepository repository) {
        this.repository = repository;
    }
    public List<ShiftTemplate> findAll(){
        return repository.findAll();
    }
    public ShiftTemplate findById(Integer id){
        return repository.findById(id)
                .orElseThrow(()->new RuntimeException("Shift not found: "+id));
    }
    public ShiftTemplate create(ShiftTemplate shift) {
        if (repository.findByShiftCode(shift.getShiftCode()).isPresent()) {
            throw new RuntimeException("Shift code already exists: " + shift.getShiftCode());
        }
        if (shift.getIsActive() == null) shift.setIsActive(true);
        if (shift.getIsOvernight() == null) shift.setIsOvernight(false);
        return repository.save(shift);
    }
    public ShiftTemplate update(Integer id, ShiftTemplate req) {
        ShiftTemplate cur = findById(id);
        cur.setShiftName(req.getShiftName());
        cur.setStartTime(req.getStartTime());
        cur.setEndTime(req.getEndTime());
        cur.setIsOvernight(req.getIsOvernight());
        cur.setIsActive(req.getIsActive());
        return repository.save(cur);
    }
    public void deactivate(Integer id) {
        ShiftTemplate cur = findById(id);
        cur.setIsActive(false);
        repository.save(cur);
    }
}
