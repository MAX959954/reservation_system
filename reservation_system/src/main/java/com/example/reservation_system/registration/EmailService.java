package com.example.reservation_system.registration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private static final String SENDER = "no-reply@gmail.com";  // Update to match your configured username if needed

    public EmailService (JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void send(String to, String subject, String text) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(text)) {
            log.error("Invalid parameters: to={} subject={} text={}", to, subject, text);
            throw new IllegalArgumentException("Invalid parameters");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);  // 'true' enables multipart/HTML
            helper.setFrom(SENDER);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);  // 'true' indicates HTML content
            mailSender.send(message);
            log.info("Email sent");
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}