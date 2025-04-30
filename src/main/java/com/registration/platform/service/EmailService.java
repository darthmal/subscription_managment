package com.registration.platform.service;

import java.util.Map;

public interface EmailService {

    /**
     * Sends a simple plain text email.
     *
     * @param to      The recipient's email address.
     * @param subject The email subject.
     * @param text    The plain text content of the email.
     */
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * Sends an email using an HTML template (e.g., Thymeleaf, FreeMarker, or pre-rendered MJML).
     * The actual template processing logic will be in the implementation.
     *
     * @param to           The recipient's email address.
     * @param subject      The email subject.
     * @param templateName The name/path of the template file.
     * @param templateModel A map containing variables to be used in the template.
     */
    void sendMessageUsingTemplate(String to, String subject, String templateName, Map<String, Object> templateModel);

    // We might add more specific methods later, e.g.:
    // void sendRegistrationConfirmationEmail(User user);
    // void sendPasswordResetEmail(User user, String resetToken);
    // void sendDocumentStatusUpdateEmail(User user, Document document);
}