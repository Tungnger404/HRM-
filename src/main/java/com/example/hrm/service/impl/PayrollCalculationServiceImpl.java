package com.example.hrm.service.impl;

import com.example.hrm.entity.PayrollBatch;
import com.example.hrm.entity.Payslip;
import com.example.hrm.entity.PayslipItem;
import com.example.hrm.repository.PayrollBatchRepository;
import com.example.hrm.repository.PayslipItemRepository;
import com.example.hrm.repository.PayslipRepository;
import com.example.hrm.service.BenefitService;
import com.example.hrm.service.PayrollCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private final BenefitService benefitService;
    private final PayslipItemRepository itemRepo;
    private final PayslipRepository payslipRepo;
    private final PayrollBatchRepository batchRepo;

    @Override
    public PayslipComputation compute(Integer empId,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      BigDecimal baseSalary,
                                      BigDecimal standardDays,
                                      BigDecimal actualDays,
                                      BigDecimal otHours) {

        BigDecimal base = nz(baseSalary).setScale(2, RoundingMode.HALF_UP);
        BigDecimal standard = nz(standardDays);
        BigDecimal actual = nz(actualDays);
        BigDecimal ot = nz(otHours);

        BigDecimal salaryByDays = BigDecimal.ZERO;
        if (standard.compareTo(BigDecimal.ZERO) > 0) {
            salaryByDays = base.multiply(actual).divide(standard, 2, RoundingMode.HALF_UP);
        }

        BigDecimal hourly = BigDecimal.ZERO;
        if (standard.compareTo(BigDecimal.ZERO) > 0) {
            hourly = base.divide(standard, 2, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        }

        BigDecimal overtimePay = hourly.multiply(ot).multiply(BigDecimal.valueOf(1.5))
                .setScale(2, RoundingMode.HALF_UP);

        List<BenefitService.BenefitApplied> benefits = (empId == null)
                ? List.of()
                : benefitService.calculateForEmployee(empId, startDate, endDate, base);

        BigDecimal benefitIncome = benefits.stream()
                .filter(x -> "INCOME".equalsIgnoreCase(x.type()))
                .map(BenefitService.BenefitApplied::amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal benefitDeduction = benefits.stream()
                .filter(x -> "DEDUCTION".equalsIgnoreCase(x.type()))
                .map(BenefitService.BenefitApplied::amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = salaryByDays.add(overtimePay).add(benefitIncome);
        BigDecimal totalDeduction = benefitDeduction;
        BigDecimal netSalary = totalIncome.subtract(totalDeduction);

        return new PayslipComputation(
                salaryByDays,
                overtimePay,
                benefitIncome,
                benefitDeduction,
                totalIncome,
                totalDeduction,
                netSalary,
                benefits
        );
    }

    @Override
    public void upsertComputedItems(Payslip payslip, PayslipComputation computation, boolean manualAdjustment) {
        List<PayslipItem> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslip.getId());

        Map<String, PayslipItem> byCode = new HashMap<>();
        for (PayslipItem it : items) {
            if (it.getItemCode() != null) {
                byCode.put(it.getItemCode().trim().toUpperCase(), it);
            }
        }

        upsertItem(byCode, payslip, "BASE_BY_DAYS", "Salary by work days",
                computation.salaryByDays(), "INCOME", manualAdjustment);

        upsertItem(byCode, payslip, "OT_PAY", "Overtime pay",
                computation.overtimePay(), "INCOME", manualAdjustment);

        for (BenefitService.BenefitApplied x : computation.benefits()) {
            BigDecimal amt = x.amount() == null ? BigDecimal.ZERO : x.amount();
            upsertItem(byCode, payslip, x.code(), x.name(), amt, x.type(), manualAdjustment);
        }

        Set<String> keep = new HashSet<>();
        keep.add("BASE_BY_DAYS");
        keep.add("OT_PAY");
        for (BenefitService.BenefitApplied x : computation.benefits()) {
            keep.add(x.code().trim().toUpperCase());
        }

        List<PayslipItem> toDelete = new ArrayList<>();
        for (PayslipItem it : items) {
            String code = (it.getItemCode() == null) ? "" : it.getItemCode().trim().toUpperCase();
            if (!keep.contains(code)) {
                toDelete.add(it);
                byCode.remove(code);
            }
        }

        if (!toDelete.isEmpty()) {
            itemRepo.deleteAll(toDelete);
        }

        itemRepo.saveAll(byCode.values());
    }

    @Override
    public void recalcPayslipTotalsFromItems(Payslip payslip) {
        List<PayslipItem> items = itemRepo.findByPayslip_IdOrderByIdAsc(payslip.getId());

        BigDecimal totalIncome = items.stream()
                .filter(x -> "INCOME".equalsIgnoreCase(x.getItemType()))
                .map(x -> x.getAmount() == null ? BigDecimal.ZERO : x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeduction = items.stream()
                .filter(x -> "DEDUCTION".equalsIgnoreCase(x.getItemType()))
                .map(x -> x.getAmount() == null ? BigDecimal.ZERO : x.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        payslip.setTotalIncome(totalIncome);
        payslip.setTotalDeduction(totalDeduction);
        payslip.setNetSalary(totalIncome.subtract(totalDeduction));
        payslipRepo.save(payslip);
    }

    @Override
    public void recalcBatchTotals(PayrollBatch batch) {
        if (batch == null) return;

        List<Payslip> slips = payslipRepo.findByBatch_IdOrderByIdAsc(batch.getId());

        BigDecimal grossSum = slips.stream()
                .filter(s -> !"REJECTED".equalsIgnoreCase(s.getSlipStatus()))
                .map(s -> s.getTotalIncome() == null ? BigDecimal.ZERO : s.getTotalIncome())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSum = slips.stream()
                .filter(s -> !"REJECTED".equalsIgnoreCase(s.getSlipStatus()))
                .map(s -> s.getNetSalary() == null ? BigDecimal.ZERO : s.getNetSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        batch.setTotalGross(grossSum);
        batch.setTotalNet(netSum);
        batchRepo.save(batch);
    }

    private void upsertItem(Map<String, PayslipItem> byCode,
                            Payslip payslip,
                            String code,
                            String name,
                            BigDecimal amount,
                            String type,
                            boolean manualAdjustment) {

        String key = code.trim().toUpperCase();
        PayslipItem it = byCode.get(key);

        if (it == null) {
            it = PayslipItem.builder()
                    .payslip(payslip)
                    .itemCode(code)
                    .itemName(name)
                    .itemType(type)
                    .manualAdjustment(manualAdjustment)
                    .amount(BigDecimal.ZERO)
                    .build();
            byCode.put(key, it);
        }

        it.setAmount(amount == null ? BigDecimal.ZERO : amount);
        it.setItemName(name);
        it.setItemType(type);
        it.setManualAdjustment(manualAdjustment);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}