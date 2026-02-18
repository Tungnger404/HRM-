# Module Integration Guide: Evaluation → Payroll

## Overview
This guide explains how **Module 4 (Payroll)** can integrate with **Module 5 (Evaluation & Training)** to calculate performance bonuses based on employee evaluation results.

---

## 🔗 Integration Interface

### **Service Method**
```java
com.example.hrm.service.EvaluationService
```

### **Available Methods**

#### 1. Get evaluation result by Employee ID and Cycle ID
```java
EvaluationResultDTO getEvaluationResultForPayroll(Integer employeeId, Integer cycleId);
```

**Usage:**
```java
@Autowired
private EvaluationService evaluationService;

EvaluationResultDTO result = evaluationService.getEvaluationResultForPayroll(employeeId, cycleId);
if (result != null) {
    String classification = result.getClassification(); // "A", "B", "C", "D"
    BigDecimal bonusPercentage = result.getSuggestedBonusPercentage(); // 0.20, 0.10, 0.00, -0.05
}
```

---

#### 2. Get evaluation result by Period (Year/Month)
```java
EvaluationResultDTO getEvaluationResultByPeriod(Integer employeeId, Integer year, Integer month);
```

**Usage:**
```java
// For payroll period: January 2024
EvaluationResultDTO result = evaluationService.getEvaluationResultByPeriod(employeeId, 2024, 1);
```

---

#### 3. Get latest completed evaluation
```java
EvaluationResultDTO getLatestEvaluationResult(Integer employeeId);
```

**Usage:**
```java
// Fallback when cycle/period is unknown
EvaluationResultDTO result = evaluationService.getLatestEvaluationResult(employeeId);
```

---

## 📊 Response DTO Structure

### **EvaluationResultDTO**
```java
{
    "employeeId": 123,
    "employeeName": "Nguyen Van A",
    "evaluationId": 456,
    "cycleId": 1,
    "cycleName": "Q4 2023",
    "selfScore": 92.0,
    "managerScore": 95.0,
    "totalScore": 93.5,
    "classification": "A",                    // "A", "B", "C", "D"
    "classificationLabel": "Excellent",       // "Excellent", "Good", "Average", "Poor"
    "status": "COMPLETED",
    "completedAt": "2024-01-15T10:30:00",
    "suggestedBonusPercentage": 0.20          // 20% for A, 10% for B, 0% for C, -5% for D
}
```

---

## 💰 Bonus Calculation Logic

### **Suggested Bonus Percentages**

| Classification | Label | Bonus Percentage | Description |
|----------------|-------|------------------|-------------|
| **A** | Excellent | **+20%** | High performance, exceptional work |
| **B** | Good | **+10%** | Above average performance |
| **C** | Average | **0%** | Meets expectations |
| **D** | Poor | **-5%** | Below expectations (penalty) |

---

## 🔧 Integration into PayrollService

### **Example Implementation**

```java
@Service
public class PayrollService {
    
    @Autowired
    private EvaluationService evaluationService;
    
    @Transactional
    public Integer generatePayrollDraft(Integer periodId, Integer createdByEmpId) {
        // ... existing code ...
        
        for (Employee emp : employees) {
            // 1. Get base salary
            BigDecimal baseSalary = getBaseSalary(emp);
            
            // 2. Get attendance data
            BigDecimal salaryByDays = calculateSalaryByDays(emp, baseSalary);
            
            // 3. Get overtime pay
            BigDecimal overtimePay = calculateOvertimePay(emp);
            
            // 4. ✨ NEW: Get performance bonus from evaluation
            BigDecimal performanceBonus = BigDecimal.ZERO;
            try {
                EvaluationResultDTO evalResult = evaluationService.getEvaluationResultByPeriod(
                    emp.getId(), 
                    period.getYear(), 
                    period.getMonth()
                );
                
                if (evalResult != null && evalResult.getSuggestedBonusPercentage() != null) {
                    performanceBonus = baseSalary
                        .multiply(evalResult.getSuggestedBonusPercentage())
                        .setScale(2, RoundingMode.HALF_UP);
                }
            } catch (Exception e) {
                // Log error, continue without bonus
                log.warn("Could not retrieve evaluation for employee {}: {}", emp.getId(), e.getMessage());
            }
            
            // 5. Calculate total income
            BigDecimal totalIncome = salaryByDays.add(overtimePay).add(performanceBonus);
            
            // 6. Create payslip items
            List<PayslipItem> items = new ArrayList<>();
            
            // ... existing items (salary by days, overtime) ...
            
            // Add performance bonus item if exists
            if (performanceBonus.compareTo(BigDecimal.ZERO) != 0) {
                items.add(PayslipItem.builder()
                    .payslip(slip)
                    .itemCode("PERFORMANCE_BONUS")
                    .itemName("Performance Bonus (" + evalResult.getClassification() + ")")
                    .amount(performanceBonus)
                    .itemType(performanceBonus.compareTo(BigDecimal.ZERO) > 0 ? "INCOME" : "DEDUCTION")
                    .manualAdjustment(false)
                    .build());
            }
            
            // ... rest of the code ...
        }
    }
}
```

---

## ⚠️ Error Handling

### **Scenarios to Handle**

1. **No evaluation found**
   - `getEvaluationResultForPayroll()` returns `null`
   - Action: Continue payroll without bonus

2. **Evaluation not completed**
   - Only `COMPLETED` evaluations are returned
   - Action: Continue payroll without bonus

3. **Service exception**
   - Catch exception and log warning
   - Action: Continue payroll without bonus (don't fail entire payroll)

### **Example**
```java
try {
    EvaluationResultDTO result = evaluationService.getLatestEvaluationResult(employeeId);
    if (result != null) {
        // Apply bonus
    } else {
        log.info("No completed evaluation found for employee {}, skipping bonus", employeeId);
    }
} catch (Exception e) {
    log.error("Error retrieving evaluation for employee {}: {}", employeeId, e.getMessage());
    // Continue without bonus
}
```

---

## 🧪 Testing

### **Test Scenarios**

1. **Employee with A classification**
   - Expected: +20% bonus in payslip

2. **Employee with B classification**
   - Expected: +10% bonus in payslip

3. **Employee with C classification**
   - Expected: No bonus

4. **Employee with D classification**
   - Expected: -5% penalty (deduction item)

5. **Employee without evaluation**
   - Expected: Base salary only, no bonus/penalty

---

## 📞 Contact

For questions about integration, contact:
- **Module 5 Owner:** [Your Name] - Evaluation & Training
- **GitHub PR:** [Link to your PR]

---

## 🔄 Future Enhancements

Potential improvements for future iterations:

1. **Training completion bonus**
   - Bonus for completing required training courses

2. **Multi-cycle bonuses**
   - Additional bonus for consecutive excellent performance

3. **Department-specific bonus rules**
   - Different bonus percentages per department

4. **Bonus caps**
   - Maximum bonus amount limits

---

*Last updated: [Current Date]*
*Module 5 Version: 1.0*
