package com.registration.platform.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.registration.platform.service.EmailService; // Import Async

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    // Inject template engine if using one (e.g., Thymeleaf)
    // private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}") // Get sender address from properties
    private String fromAddress;

    @Override
    @Async // Execute this method asynchronously
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            log.info("Attempting to send simple email to: {}", to);
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage(), e);
            // Handle exception (e.g., log, queue for retry)
        }
    }

    @Override
    @Async // Execute this method asynchronously
    public void sendMessageUsingTemplate(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, // Allows for inline images etc.
                    StandardCharsets.UTF_8.name());

            // --- Template Processing Logic (Placeholder) ---
            // 1. Load MJML template content (e.g., from resources/templates/email/{templateName}.mjml)
            // String mjmlContent = loadTemplateContent(templateName + ".mjml");

            // 2. Inject dynamic data from templateModel into MJML (using Thymeleaf, String replace, etc.)
            // String processedMjml = processTemplate(mjmlContent, templateModel); // Example using Thymeleaf on MJML

            // 3. Render MJML to HTML (using MJML engine/library/CLI)
            // String htmlContent = renderMjmlToHtml(processedMjml);

            // --- OR --- (Simpler approach: Use Thymeleaf directly on HTML templates)
            // 1. Create Thymeleaf context
            // Context context = new Context();
            // context.setVariables(templateModel);
            // 2. Process HTML template (e.g., resources/templates/email/{templateName}.html)
            // String htmlContent = templateEngine.process("email/" + templateName, context);

            // --- For now, using a simple placeholder HTML ---
            String htmlContent = "<h1>" + subject + "</h1><p>This is a placeholder for the template content.</p><p>Data: " + templateModel.toString() + "</p>";
            // --- End Placeholder ---


            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(fromAddress);
            helper.setText(htmlContent, true); // true indicates HTML content

            // Optional: Add attachments or inline elements if needed
            // helper.addInline("logo.png", new ClassPathResource("static/images/logo.png"));

            log.info("Attempting to send template email '{}' to: {}", templateName, to);
            mailSender.send(mimeMessage);
            log.info("Template email '{}' sent successfully to: {}", templateName, to);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send template email '{}' to {}: {}", templateName, to, e.getMessage(), e);
            // Handle exception
        }
    }

    // Placeholder methods for template loading/processing - replace with actual implementation
    // private String loadTemplateContent(String templatePath) { /* ... */ return ""; }
    // private String processTemplate(String content, Map<String, Object> model) { /* ... */ return ""; }
    // private String renderMjmlToHtml(String mjml) { /* ... */ return ""; }

}