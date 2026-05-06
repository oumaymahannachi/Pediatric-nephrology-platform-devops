package tn.pedialink.labresults.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tn.pedialink.labresults.dto.NotificationRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:PediaLink}")
    private String appName;

    @Autowired
    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendLabResultNotification(NotificationRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getRecipientEmail());
            helper.setSubject("🔬 New Lab Result Available - " + appName);

            String htmlContent = buildEmailContent(request);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email notification sent successfully to: {}", request.getRecipientEmail());

        } catch (MessagingException e) {
            log.error("Failed to send email notification to: {}", request.getRecipientEmail(), e);
        }
    }

    @Async
    public void sendCriticalResultAlert(NotificationRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getRecipientEmail());
            helper.setSubject("⚠️ URGENT: Critical Lab Result - " + appName);

            String htmlContent = buildCriticalAlertContent(request);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Critical alert email sent successfully to: {}", request.getRecipientEmail());

        } catch (MessagingException e) {
            log.error("Failed to send critical alert email to: {}", request.getRecipientEmail(), e);
        }
    }

    private String buildEmailContent(NotificationRequest request) {
        String statusColor = request.isAbnormal() ? "#ef4444" : "#10b981";
        String statusText = request.isAbnormal() ? "Needs Attention" : "Normal";
        String statusIcon = request.isAbnormal() ? "⚠️" : "✅";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .result-card { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
                    .status-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; font-weight: bold; background: %s; color: white; }
                    .info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #e5e7eb; }
                    .info-label { font-weight: 600; color: #6b7280; }
                    .info-value { color: #111827; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; margin-top: 20px; font-weight: 600; }
                    .footer { text-align: center; color: #9ca3af; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔬 %s</h1>
                        <p>New Laboratory Test Result Available</p>
                    </div>
                    <div class="content">
                        <p>Dear Parent,</p>
                        <p>A new laboratory test result has been added for <strong>%s</strong> by Dr. %s.</p>
                        
                        <div class="result-card">
                            <div class="info-row">
                                <span class="info-label">Test Type:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Test Date:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Status:</span>
                                <span class="status-badge">%s %s</span>
                            </div>
                        </div>

                        <p>Please log in to your PediaLink account to view the complete results and any recommendations from your doctor.</p>
                        
                        <center>
                            <a href="http://localhost:4200/parent/lab-results" class="cta-button">View Lab Results</a>
                        </center>

                        <p style="margin-top: 30px; font-size: 14px; color: #6b7280;">
                            <strong>Note:</strong> If you have any questions or concerns about these results, please contact your doctor directly.
                        </p>
                    </div>
                    <div class="footer">
                        <p>This is an automated notification from %s</p>
                        <p>Please do not reply to this email</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(statusColor, appName, request.getPatientName(), request.getDoctorName(),
                    request.getTestType(), request.getTestDate(), statusIcon, statusText, appName);
    }

    private String buildCriticalAlertContent(NotificationRequest request) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc2626; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #fef2f2; padding: 30px; border-radius: 0 0 10px 10px; border: 3px solid #dc2626; }
                    .alert-box { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 5px solid #dc2626; }
                    .cta-button { display: inline-block; background: #dc2626; color: white; padding: 15px 40px; text-decoration: none; border-radius: 8px; margin-top: 20px; font-weight: bold; font-size: 16px; }
                    .footer { text-align: center; color: #9ca3af; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ URGENT ALERT</h1>
                        <p>Critical Lab Result Requires Immediate Attention</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <h2 style="color: #dc2626; margin-top: 0;">⚠️ Critical Result Detected</h2>
                            <p><strong>Patient:</strong> %s</p>
                            <p><strong>Test Type:</strong> %s</p>
                            <p><strong>Test Date:</strong> %s</p>
                            <p><strong>Doctor:</strong> Dr. %s</p>
                        </div>

                        <p style="font-size: 16px; font-weight: 600; color: #dc2626;">
                            This laboratory result contains abnormal values that require immediate medical attention.
                        </p>

                        <p><strong>IMPORTANT:</strong> Please contact your doctor as soon as possible or visit the emergency room if you notice any concerning symptoms.</p>
                        
                        <center>
                            <a href="http://localhost:4200/parent/lab-results" class="cta-button">VIEW RESULTS NOW</a>
                        </center>

                        <p style="margin-top: 30px; padding: 15px; background: white; border-radius: 8px;">
                            <strong>Emergency Contact:</strong><br>
                            If this is a medical emergency, please call emergency services immediately or go to the nearest emergency room.
                        </p>
                    </div>
                    <div class="footer">
                        <p>This is an automated critical alert from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(request.getPatientName(), request.getTestType(), request.getTestDate(),
                    request.getDoctorName(), appName);
    }
}
