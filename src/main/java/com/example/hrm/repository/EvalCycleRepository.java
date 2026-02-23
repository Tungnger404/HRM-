package com.example.hrm.repository;

import com.example.hrm.entity.EvalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvalCycleRepository extends JpaRepository<EvalCycle, Integer> {

    List<EvalCycle> findByIsActiveTrue();

    Optional<EvalCycle> findByCycleName(String cycleName);

    List<EvalCycle> findAllByOrderByStartDateDesc();
}