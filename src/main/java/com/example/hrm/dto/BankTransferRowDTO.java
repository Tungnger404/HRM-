package com.example.hrm.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankTransferRowDTO {

    private Integer empId;
    private String employeeName;

    private BigDecimal netTotal;

    private String bankName;
    private String branchName;
    private String accountNumber;
    private String holderName;

    // nếu thiếu bank account
    private String note;
}
