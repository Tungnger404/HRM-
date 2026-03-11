package com.example.hrm.service.impl;

import com.example.hrm.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // =====================================================
    // ================= INTERVIEW MAIL ====================
    // =====================================================

    @Override
    public void sendInterviewMail(String to,
                                  String name,
                                  String jobTitle,
                                  LocalDateTime date,
                                  String location) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject("Interview Invitation – " + jobTitle);

        String formattedDate = date != null
                ? date.format(dateTimeFormatter)
                : "To be announced";

        message.setText(
                "Dear " + name + ",\n\n" +
                        "Thank you for applying for the position of " + jobTitle + ".\n\n" +
                        "We are pleased to invite you to attend an interview with the details below:\n\n" +
                        "Position: " + jobTitle + "\n" +
                        "Date & Time: " + formattedDate + "\n" +
                        "Location: " + location + "\n\n" +
                        "Please confirm your availability by replying to this email.\n\n" +
                        "Best regards,\n" +
                        "Human Resources Department"
        );

        mailSender.send(message);
    }

    // =====================================================
    // ================= REJECT MAIL =======================
    // =====================================================

    @Override
    public void sendRejectMail(String to,
                               String name,
                               String jobTitle) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject("Recruitment Update – " + jobTitle);

        message.setText(
                "Dear " + name + ",\n\n" +
                        "Thank you for your interest in the position of " + jobTitle + ".\n\n" +
                        "After careful consideration, we regret to inform you that " +
                        "we will not be proceeding with your application at this time.\n\n" +
                        "We sincerely appreciate your effort and wish you success in your career journey.\n\n" +
                        "Best regards,\n" +
                        "Human Resources Department"
        );

        mailSender.send(message);
    }

    // =====================================================
    // ================= OFFER MAIL ========================
    // =====================================================

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
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject("Official Job Offer – " + jobTitle);

        String formattedSalary = salary != null
                ? NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(salary)
                : "Negotiable";

        String formattedStartDate = startDate != null
                ? startDate.format(dateFormatter)
                : "To be confirmed";

        message.setText(
                "Dear " + name + ",\n\n" +
                        "We are delighted to offer you the position of " + jobTitle + ".\n\n" +
                        "Offer Details:\n" +
                        "Salary: " + formattedSalary + "\n" +
                        "Start Date: " + formattedStartDate + "\n" +
                        "Probation Period: " + probation + "\n\n" +
                        "Please confirm your decision by clicking one of the links below:\n\n" +
                        "Accept Offer:\n" + acceptLink + "\n\n" +
                        "Reject Offer:\n" + rejectLink + "\n\n" +
                        "We look forward to welcoming you to our team.\n\n" +
                        "Best regards,\n" +
                        "Human Resources Department"
        );

        mailSender.send(message);
    }
}