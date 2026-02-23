package com.example.hrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "is_primary")
    private Boolean isPrimary;
}
