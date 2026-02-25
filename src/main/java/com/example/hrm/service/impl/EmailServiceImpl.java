package com.example.hrm.service.impl;

import com.example.hrm.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInterviewMail(String to,
                                  String name,
                                  String jobTitle,
                                  LocalDateTime date,
                                  String location) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Interview Invitation - " + jobTitle);

        message.setText(
                "Dear " + name + ",\n\n" +
                        "You are invited to interview for: " + jobTitle + "\n\n" +
                        "Time: " + date + "\n" +
                        "Location: " + location + "\n\n" +
                        "Best regards,\nHR Team"
        );

        mailSender.send(message);
    }
    @Override
    public void sendRejectMail(String to,
                               String name,
                               String jobTitle) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Application Result - " + jobTitle);

        message.setText(
                "Dear " + name + ",\n\n" +
                        "Thank you for applying for the position: " + jobTitle + ".\n\n" +
                        "After careful consideration, we regret to inform you that " +
                        "you were not selected for this role.\n\n" +
                        "We wish you success in your future career.\n\n" +
                        "Best regards,\nHR Team"
        );

        mailSender.send(message);
    }
}