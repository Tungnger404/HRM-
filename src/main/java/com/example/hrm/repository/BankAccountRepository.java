package com.example.hrm.repository;

import com.example.hrm.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {

    Optional<BankAccount> findFirstByEmpIdAndIsPrimaryTrue(Integer empId);

    List<BankAccount> findByEmpIdInAndIsPrimaryTrue(List<Integer> empIds);

    List<BankAccount> findByEmpId(Integer empId);
}
