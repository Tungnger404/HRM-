package com.example.hrm.service.impl;

import com.example.hrm.dto.BenefitUpsertDTO;
import com.example.hrm.entity.Benefit;
import com.example.hrm.entity.Employee;
import com.example.hrm.entity.EmployeeBenefit;
import com.example.hrm.repository.BenefitRepository;
import com.example.hrm.repository.EmployeeBenefitRepository;
import com.example.hrm.repository.EmployeeRepository;
import com.example.hrm.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepo;
    private final EmployeeBenefitRepository employeeBenefitRepo;
    private final EmployeeRepository employeeRepo;

    @Override
    @Transactional(readOnly = true)
    public List<Benefit> list(String q, String type) {
        return benefitRepo.search(q, type);
    }

    /**
     * Tạo mới một benefit.
     *
     * @param dto dữ liệu đầu vào
     * @return benefit đã lưu
     */
    @Override
    @Transactional
    public Benefit create(BenefitUpsertDTO dto) {
        validate(dto, true);

        Benefit b = Benefit.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .type(dto.getType())
                .calcMethod(dto.getCalcMethod())
                .value(nz(dto.getValue()))
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .active(Boolean.TRUE.equals(dto.getActive()))
                .build();

        return benefitRepo.save(b);
    }

    /**
     * Cập nhật benefit.
     *
     * @param id  id benefit
     * @param dto dữ liệu cập nhật
     * @return benefit sau khi update
     */
    @Override
    @Transactional
    public Benefit update(Integer id, BenefitUpsertDTO dto) {
        validate(dto, false);

        Benefit b = benefitRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found: " + id));

        b.setCode(dto.getCode().trim().toUpperCase());
        b.setName(dto.getName().trim());
        b.setType(dto.getType());
        b.setCalcMethod(dto.getCalcMethod());
        b.setValue(nz(dto.getValue()));
        b.setActive(Boolean.TRUE.equals(dto.getActive()));

        // edit không còn field From/To trên UI thì giữ nguyên giá trị cũ
        if (dto.getEffectiveFrom() != null) {
            b.setEffectiveFrom(dto.getEffectiveFrom());
        }
        if (dto.getEffectiveTo() != null) {
            b.setEffectiveTo(dto.getEffectiveTo());
        }

        return benefitRepo.save(b);
    }

    /**
     * Xóa benefit theo id.
     *
     * @param id id benefit
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        benefitRepo.deleteById(id);
    }

    /**
     * Gán một benefit cho toàn bộ nhân viên đang làm việc.
     *
     * @param benefitId     id benefit
     * @param effectiveFrom ngày bắt đầu áp dụng
     * @param effectiveTo   ngày kết thúc
     * @param overrideValue giá trị override (nếu có)
     */
    @Override
    @Transactional
    public void assignBenefitToAllEmployees(Integer benefitId,
                                            LocalDate effectiveFrom,
                                            LocalDate effectiveTo,
                                            BigDecimal overrideValue) {

        Benefit b = benefitRepo.findById(benefitId)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found: " + benefitId));

        List<Employee> employees = employeeRepo.findAll().stream()
                .filter(e -> e.getStatus() == null || !List.of("RESIGNED", "TERMINATED").contains(e.getStatus()))
                .toList();

        for (Employee emp : employees) {
            boolean existed = employeeBenefitRepo.existsByEmployee_IdAndBenefit_IdAndEffectiveFrom(
                    emp.getId(), b.getId(), effectiveFrom
            );
            if (existed) continue;

            EmployeeBenefit eb = EmployeeBenefit.builder()
                    .employee(emp)
                    .benefit(b)
                    .overrideValue(overrideValue)
                    .effectiveFrom(effectiveFrom)
                    .effectiveTo(effectiveTo)
                    .active(true)
                    .build();

            employeeBenefitRepo.save(eb);
        }
    }

    /**
     * Tính toán tất cả benefit áp dụng cho một nhân viên trong khoảng thời gian.
     *
     * @param empId          id nhân viên
     * @param start          ngày bắt đầu
     * @param end            ngày kết thúc
     * @param baseSalary     lương cơ bản
     * @param actualWorkDays số ngày làm thực tế
     * @return danh sách benefit đã tính
     */
    @Override
    @Transactional(readOnly = true)
    public List<BenefitApplied> calculateForEmployee(Integer empId,
                                                     LocalDate start,
                                                     LocalDate end,
                                                     BigDecimal baseSalary,
                                                     BigDecimal actualWorkDays) {
        BigDecimal base = nz(baseSalary);
        BigDecimal actual = nz(actualWorkDays);

        List<EmployeeBenefit> rows = employeeBenefitRepo.findEffectiveForEmployee(empId, start, end);

        return rows.stream().map(eb -> {
            Benefit b = eb.getBenefit();
            BigDecimal v = (eb.getOverrideValue() != null) ? eb.getOverrideValue() : nz(b.getValue());

            BigDecimal amount;
            if ("PERCENT_BASE".equalsIgnoreCase(b.getCalcMethod())) {
                amount = base.multiply(v).setScale(2, RoundingMode.HALF_UP);
            } else if ("PER_WORKING_DAY".equalsIgnoreCase(b.getCalcMethod())) {
                amount = v.multiply(actual).setScale(2, RoundingMode.HALF_UP);
            } else {
                amount = v.setScale(2, RoundingMode.HALF_UP);
            }

            return new BenefitApplied(b.getCode(), b.getName(), b.getType(), amount);
        }).collect(Collectors.toList());
    }

    /**
     * Validate dữ liệu đầu vào.
     *
     * @param dto dữ liệu
     * @param isCreate true nếu là create
     */
    private void validate(BenefitUpsertDTO dto, boolean isCreate) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (!List.of("INCOME", "DEDUCTION").contains(String.valueOf(dto.getType()))) {
            throw new IllegalArgumentException("Type must be INCOME or DEDUCTION");
        }

        if (!List.of("FIXED", "PERCENT_BASE", "PER_WORKING_DAY").contains(String.valueOf(dto.getCalcMethod()))) {
            throw new IllegalArgumentException("CalcMethod must be FIXED, PERCENT_BASE or PER_WORKING_DAY");
        }

        // chỉ CREATE mới bắt buộc effectiveFrom
        if (isCreate && dto.getEffectiveFrom() == null) {
            throw new IllegalArgumentException("EffectiveFrom is required");
        }

        if (dto.getValue() == null) {
            dto.setValue(BigDecimal.ZERO);
        }

        if (isCreate && benefitRepo.existsByCodeIgnoreCase(dto.getCode().trim())) {
            throw new IllegalArgumentException("Code already exists");
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * Lấy danh sách benefit đang active.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Benefit> listActive() {
        return benefitRepo.findByActiveTrueOrderByTypeAscCodeAsc();
    }

    /**
     * Kiểm tra benefit có active không.
     *
     * @param id id benefit
     * @return benefit hợp lệ
     */
    @Override
    @Transactional(readOnly = true)
    public Benefit requireActive(Integer id) {
        Benefit b = benefitRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Benefit not found: " + id));
        if (!Boolean.TRUE.equals(b.getActive())) {
            throw new IllegalStateException("Benefit đang NGỪNG hoạt động nên không thể thêm vào payslip.");
        }
        return b;
    }
}