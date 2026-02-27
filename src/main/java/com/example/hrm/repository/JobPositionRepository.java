package com.example.hrm.repository;

import com.example.hrm.entity.JobPosition;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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
}