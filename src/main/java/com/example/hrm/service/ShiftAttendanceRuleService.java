package com.example.hrm.service;

import com.example.hrm.entity.ShiftAttendanceRule;
import com.example.hrm.entity.ShiftTemplate;
import com.example.hrm.repository.ShiftAttendanceRuleRepository;
import com.example.hrm.repository.ShiftTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

    @Service
    public class ShiftAttendanceRuleService {

        private final ShiftAttendanceRuleRepository ruleRepository;
        private final ShiftTemplateRepository shiftRepository;

        public ShiftAttendanceRuleService(ShiftAttendanceRuleRepository ruleRepository,
                                          ShiftTemplateRepository shiftRepository) {
            this.ruleRepository = ruleRepository;
            this.shiftRepository = shiftRepository;
        }

        public List<ShiftAttendanceRule> findAll() {
            return ruleRepository.findAll();
        }

        public ShiftAttendanceRule getByShiftCode(String shiftCode) {
            return ruleRepository.findByShiftTemplate_ShiftCode(shiftCode)
                    .orElseThrow(() -> new RuntimeException("Rule not found for shift: " + shiftCode));
        }

        public ShiftAttendanceRule upsertByShiftCode(String shiftCode, Integer earlyMinutes, Integer lateMinutes, Boolean isActive) {
            validate(earlyMinutes, lateMinutes);

            ShiftTemplate shift = shiftRepository.findByShiftCode(shiftCode)
                    .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftCode));

            ShiftAttendanceRule rule = ruleRepository.findByShiftTemplate_ShiftId(shift.getShiftId())
                    .orElse(ShiftAttendanceRule.builder().shiftTemplate(shift).build());

            rule.setEarlyCheckinMinutes(earlyMinutes);
            rule.setLateThresholdMinutes(lateMinutes);
            rule.setIsActive(isActive == null ? true : isActive);

            return ruleRepository.save(rule);
        }

        private void validate(Integer earlyMinutes, Integer lateMinutes) {
            if (earlyMinutes == null || earlyMinutes < 0) {
                throw new RuntimeException("early_checkin_minutes must be >= 0");
            }
            if (lateMinutes == null || lateMinutes < 0) {
                throw new RuntimeException("late_threshold_minutes must be >= 0");
            }
        }
    }

