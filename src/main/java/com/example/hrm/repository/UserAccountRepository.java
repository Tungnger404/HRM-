package com.example.hrm.repository;

import com.example.hrm.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    // ✅ Load kèm role để tránh LazyInitializationException
    @EntityGraph(attributePaths = "role")
    Optional<UserAccount> findByUsername(String username);
}
