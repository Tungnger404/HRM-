package com.example.hrm.repository;

import com.example.hrm.entity.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    @EntityGraph(attributePaths = "role")
    Optional<UserAccount> findByUsername(String username);

    @EntityGraph(attributePaths = "role")
    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "role")
    Optional<UserAccount> findByEmail(String email);

    @EntityGraph(attributePaths = "role")
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "role")
    Optional<UserAccount> findByGoogleId(String googleId);

    @EntityGraph(attributePaths = "role")
    java.util.List<UserAccount> findByRole_RoleNameIgnoreCaseAndActiveTrue(String roleName);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);


}