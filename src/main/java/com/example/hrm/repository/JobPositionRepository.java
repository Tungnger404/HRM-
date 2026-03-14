package com.example.hrm.repository;

import com.example.hrm.entity.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobPositionRepository extends JpaRepository<JobPosition, Integer> {

    @Query("""
    select jp from JobPosition jp
    where (:q is null or :q = '' or lower(jp.title) like lower(concat('%', :q, '%')))
      and (:level is null or jp.jobLevel = :level)
      and (:active is null or jp.active = :active)
    """)
    Page<JobPosition> search(@Param("q") String q,
                             @Param("level") Integer level,
                             @Param("active") Boolean active,
                             Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);

    List<JobPosition> findByActiveTrueOrderByTitleAsc();
}