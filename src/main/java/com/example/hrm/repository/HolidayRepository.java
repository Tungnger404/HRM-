package com.example.hrm.repository;

import com.example.hrm.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query("SELECT h FROM Holiday h WHERE h.status = 'ACTIVE' AND h.holidayDate >= CURRENT_DATE ORDER BY h.holidayDate ASC")
    List<Holiday> findUpcomingActiveHolidays();

    List<Holiday> findAllByOrderByHolidayDateDesc();

    List<Holiday> findByStatusAndHolidayDateBetween(String status, LocalDate start, LocalDate end);
}
