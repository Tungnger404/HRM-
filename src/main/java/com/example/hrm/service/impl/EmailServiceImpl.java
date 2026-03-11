package com.example.hrm.service.impl;

import com.example.hrm.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromMail;

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
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject("Invitation to Interview – " + jobTitle);

        message.setText(
                "Dear " + name + ",\n\n" +
                        "Thank you for your interest in joining Software House.\n\n" +
                        "We are pleased to inform you that your application for the position of "
                        + jobTitle + " has been shortlisted by our recruitment team.\n\n" +
                        "You are invited to attend an interview with us. Please find the details below:\n\n" +
                        "Position: " + jobTitle + "\n" +
                        "Date & Time: " + date + "\n" +
                        "Location: " + location + "\n\n" +
                        "Kindly confirm your availability by replying to this email.\n\n" +
                        "If you have any questions, please feel free to contact the HR Department via the HRM System.\n\n" +
                        "We look forward to meeting you and discussing your potential contribution to Software House.\n\n" +
                        "Best regards,\n" +
                        "Human Resources Department\n" +
                        "Software House\n" +
                        "HRM System"
        );

        mailSender.send(message);
    }

    @Override
    public void sendRejectMail(String to,
                               String name,
                               String jobTitle) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject("Recruitment Update – " + jobTitle);

        message.setText(
                "Dear " + name + ",\n\n" +
                        "Thank you for your interest in the position of " + jobTitle +
                        " at Software House and for participating in our recruitment process.\n\n" +
                        "After careful consideration, we regret to inform you that " +
                        "we will not be moving forward with your application at this time.\n\n" +
                        "We sincerely appreciate the effort you have put into your application. " +
                        "We encourage you to follow future opportunities at Software House through our HRM System.\n\n" +
                        "We wish you continued success in your career journey.\n\n" +
                        "Best regards,\n" +
                        "Human Resources Department\n" +
                        "Software House\n" +
                        "HRM System"
        );

        mailSender.send(message);
    }

    @Override
    public void sendOfferMail(String to,
                              String name,
                              String jobTitle,
                              Double salary,
                              LocalDate startDate,
                              String probation,
                              String acceptLink,
                              String rejectLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject("Official Job Offer – " + jobTitle);

        message.setText(
                "Dear " + name + ",\n\n" +
                        "We are pleased to offer you the position of " + jobTitle + ".\n\n" +
                        "Salary: " + salary + "\n" +
                        "Start Date: " + startDate + "\n" +
                        "Probation Period: " + probation + "\n\n" +
                        "Please confirm your decision:\n\n" +
                        "ACCEPT: " + acceptLink + "\n" +
                        "REJECT: " + rejectLink + "\n\n" +
                        "Best regards,\n" +
                        "Human Resources Department\n" +
                        "Software House"
        );

        mailSender.send(message);
    }

    @Override
    public void sendOtpEmail(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(to);
        message.setSubject("HRM - Password Reset OTP");
        message.setText(
                "Your OTP code to reset password is: " + otpCode + "\n\n" +
                        "This code will expire in 5 minutes.\n" +
                        "If you did not request this, please ignore this email."
        );
        mailSender.send(message);
    }
}