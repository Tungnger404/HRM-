package com.example.hrm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmailService {

    void sendInterviewMail(String to,
                           String name,
                           String jobTitle,
                           LocalDateTime date,
                           String location);
    void sendRejectMail(String to,
                        String name,
                        String jobTitle);
    void sendOfferMail(String to,
                       String name,
                       String jobTitle,
                       Double salary,
                       LocalDate startDate,
                       String probation,
                       String acceptLink,
                       String rejectLink);
}