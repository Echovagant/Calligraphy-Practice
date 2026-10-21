package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// @Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:书法练习平台}")
    private String appName;
    @Override
    @Async
    public void sendRegistrationConfirmationEmail(String to, String username, String confirmationToken) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("confirmationToken", confirmationToken);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/registration-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("注册确认 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("注册确认邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("注册确认邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendRegistrationWelcomeEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/registration-welcome", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("欢迎加入 " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("注册欢迎邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("注册欢迎邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendRegistrationSuccessEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/registration-success", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("注册成功 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("注册成功邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("注册成功邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetToken", resetToken);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("重置您的密码 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("密码重置邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("密码重置邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPasswordChangeConfirmationEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/password-change-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("密码修改确认 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("密码修改确认邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("密码修改确认邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPasswordResetSuccessEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/password-reset-success", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("密码重置成功 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("密码重置成功邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("密码重置成功邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAccountLockedEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/account-locked", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("账户已锁定 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("账户锁定邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("账户锁定邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAccountUnlockedEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/account-unlocked", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("账户已解锁 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("账户解锁邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("账户解锁邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendSuspiciousLoginAttemptEmail(String to, String username, String ipAddress, String location) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("location", location);
            context.setVariable("currentTime", LocalDateTime.now());
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/suspicious-login-attempt", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("可疑登录尝试提醒 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("可疑登录尝试邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("可疑登录尝试邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendLoginFromNewDeviceEmail(String to, String username, String deviceInfo, String ipAddress) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("deviceInfo", deviceInfo);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("currentTime", LocalDateTime.now());
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/login-from-new-device", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("新设备登录提醒 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("新设备登录邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("新设备登录邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendTwoFactorAuthEnabledEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/two-factor-auth-enabled", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("双重验证已启用 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("双重验证启用邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("双重验证启用邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendTwoFactorAuthDisabledEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/two-factor-auth-disabled", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("双重验证已禁用 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("双重验证禁用邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("双重验证禁用邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAdminRegistrationEmail(String to, String username, String temporaryPassword) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("temporaryPassword", temporaryPassword);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/admin-registration", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("管理员账户创建 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("管理员注册邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("管理员注册邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAdminRoleAssignedEmail(String to, String username, String roleName) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("roleName", roleName);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/admin-role-assigned", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("管理员角色分配 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("管理员角色分配邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("管理员角色分配邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAdminRoleRemovedEmail(String to, String username, String roleName) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("roleName", roleName);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/admin-role-removed", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("管理员角色移除 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("管理员角色移除邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("管理员角色移除邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAdminAccountDeactivatedEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/admin-account-deactivated", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("管理员账户已停用 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("管理员账户停用邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("管理员账户停用邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAdminAccountActivatedEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/admin-account-activated", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("管理员账户已激活 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("管理员账户激活邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("管理员账户激活邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendBulkEmail(List<String> toList, String subject, String content) {
        try {
            for (String to : toList) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject + " - " + appName);
                message.setText(content);
                message.setFrom(fromEmail);

                mailSender.send(message);
                log.info("批量邮件发送成功: {}", to);
            }

        } catch (Exception e) {
            log.error("批量邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendFileUploadConfirmationEmail(String to, String username, String fileName) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("fileName", fileName);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-upload-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("文件上传成功 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("文件上传确认邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("文件上传确认邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendFileReviewStartedEmail(String to, String filename, String reviewerName) {
        try {
            Context context = new Context();
            context.setVariable("filename", filename);
            context.setVariable("reviewerName", reviewerName);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-review-started", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("文件审核开始 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("文件审核开始邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("文件审核开始邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendFileReviewCompletedEmail(String to, String filename, String status, String reviewerComments) {
        try {
            Context context = new Context();
            context.setVariable("filename", filename);
            context.setVariable("status", status);
            context.setVariable("reviewerComments", reviewerComments);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-review-completed", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("文件审核完成 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("文件审核完成邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("文件审核完成邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendFileReviewNotificationEmail(String to, String username, String fileName, String reviewStatus) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("fileName", fileName);
            context.setVariable("reviewStatus", reviewStatus);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-review-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("文件审核结果通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("文件审核通知邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("文件审核通知邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendWorkCreatedEmail(String to, String workTitle, String workType) {
        try {
            Context context = new Context();
            context.setVariable("workTitle", workTitle);
            context.setVariable("workType", workType);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/work-created", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("作品创建成功 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("作品创建邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("作品创建邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendWorkUpdatedEmail(String to, String workTitle, String updateDescription) {
        try {
            Context context = new Context();
            context.setVariable("workTitle", workTitle);
            context.setVariable("updateDescription", updateDescription);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/work-updated", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("作品更新通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("作品更新邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("作品更新邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendWorkPublishedEmail(String to, String username, String workTitle) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("workTitle", workTitle);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/work-published", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("作品发布成功 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("作品发布邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("作品发布邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendPracticeCreatedEmail(String to, String practiceTitle, String practiceType) {
        try {
            Context context = new Context();
            context.setVariable("practiceTitle", practiceTitle);
            context.setVariable("practiceType", practiceType);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/practice-created", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("练习创建成功 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("练习创建邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("练习创建邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendPracticeUpdatedEmail(String to, String practiceTitle, String updateDescription) {
        try {
            Context context = new Context();
            context.setVariable("practiceTitle", practiceTitle);
            context.setVariable("updateDescription", updateDescription);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/practice-updated", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("练习更新通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("练习更新邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("练习更新邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPracticeReminderEmail(String to, String username, String practiceType,
            LocalDateTime scheduledTime) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("practiceType", practiceType);
            context.setVariable("scheduledTime", scheduledTime);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/practice-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("练习提醒 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("练习提醒邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("练习提醒邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendSystemMaintenanceEmail(String to, String maintenanceType, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            Context context = new Context();
            context.setVariable("maintenanceType", maintenanceType);
            context.setVariable("startTime", startTime);
            context.setVariable("endTime", endTime);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/system-maintenance", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("系统维护通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("系统维护邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("系统维护邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendSystemNotificationEmail(String to, String subject, String content,
            Map<String, Object> templateData) {
        try {
            Context context = new Context();
            context.setVariables(templateData);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/system-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject + " - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("系统通知邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("系统通知邮件发送失败: {}", e.getMessage());
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024)
            return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }
    @Override
    @Async
    public CompletableFuture<Boolean> sendBulkEmail(List<String> toList, String subject, String content,
            Map<String, Object> templateData) {
        try {
            Context context = new Context();
            context.setVariables(templateData);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/bulk-email", context);

            for (String to : toList) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(to);
                helper.setSubject(subject + " - " + appName);
                helper.setText(htmlContent, true);
                helper.setFrom(fromEmail);

                mailSender.send(message);
            }

            log.info("批量邮件发送成功，共发送 {} 封邮件", toList.size());
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException e) {
            log.error("批量邮件发送失败: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
    @Override
    public boolean validateEmailAddress(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        return email != null && email.matches(emailRegex);
    }
    @Override
    public boolean isEmailSent(String messageId) {
        return true;
    }
    @Override
    public boolean isEmailOpened(String messageId) {
        return false;
    }
    @Override
    public boolean isEmailClicked(String messageId) {
        return false;
    }
    @Override
    public java.util.Map<String, Object> getEmailStatus(String messageId) {
        return java.util.Map.of("messageId", messageId, "status", "UNKNOWN");
    }
    @Override
    public long getTotalEmailsSent() {
        return getSentEmailCount();
    }
    @Override
    public long getTotalEmailsDelivered() {
        return 0;
    }
    @Override
    public long getTotalEmailsOpened() {
        return 0;
    }
    @Override
    public long getTotalEmailsClicked() {
        return 0;
    }
    @Override
    public double getEmailDeliveryRate() {
        return 0.0;
    }
    @Override
    public double getEmailOpenRate() {
        return 0.0;
    }
    @Override
    public double getEmailClickRate() {
        return 0.0;
    }
    @Override
    public double getEmailBounceRate() {
        return 0.0;
    }
    @Override
    public void testEmailConfiguration() {
        log.info("测试邮件配置");
    }
    @Override
    public void sendTermsOfServiceUpdateEmail(String to, String username) {
    }
    @Override
    public void sendSystemUpdateEmail(String to, String username, String updateDescription) {
    }
    @Override
    public void sendPrivacyPolicyUpdateEmail(String to, String username) {
    }
    @Override
    public void sendBulkEmailWithTemplate(java.util.List<String> toList, String templateName,
            java.util.Map<String, Object> templateVariables) {
    }
    @Override
    public void sendWorkLikeEmail(String to, String username, String workTitle, String likerName) {
    }
    @Override
    public boolean isEmailServiceAvailable() {
        try {
            // 简单的邮件服务可用性检查
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setTo(fromEmail);
            testMessage.setSubject("邮件服务测试");
            testMessage.setText("邮件服务测试");
            testMessage.setFrom(fromEmail);

            mailSender.send(testMessage);
            return true;
        } catch (Exception e) {
            log.error("邮件服务不可用: {}", e.getMessage());
            return false;
        }
    }
    @Override
    public long getEmailQueueSize() {
        // 这里应该实现实际的队列大小检查
        return 0;
    }
    @Override
    public long getSentEmailCount() {
        // 这里应该实现实际的发送数量统计
        return 0;
    }
    @Override
    public long getFailedEmailCount() {
        // 这里应该实现实际的失败数量统计
        return 0;
    }
    @Override
    public double getEmailSuccessRate() {
        long sent = getSentEmailCount();
        long failed = getFailedEmailCount();
        long total = sent + failed;

        return total > 0 ? (double) sent / total * 100 : 100.0;
    }
    @Override
    public void updateEmailConfiguration(String smtpHost, int smtpPort, String username, String password) {
        // 这里应该实现邮件配置更新逻辑
        log.info("更新邮件配置: {}:{}", smtpHost, smtpPort);
    }
    @Override
    @Async
    public void sendFileApprovedEmail(String to, String username, String fileName, String fileType) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("fileName", fileName);
            context.setVariable("fileType", fileType);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-approved", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("文件审核通过 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("文件审核通过邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("文件审核通过邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendFileRejectedEmail(String to, String username, String fileName, String fileType,
            String rejectReason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("fileName", fileName);
            context.setVariable("fileType", fileType);
            context.setVariable("rejectReason", rejectReason);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-rejected", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("文件审核未通过 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("文件审核未通过邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("文件审核未通过邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPracticeSubmissionEmail(String to, String username, String practiceTitle, String submissionTime) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("practiceTitle", practiceTitle);
            context.setVariable("submissionTime", submissionTime);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/practice-submission", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("练习提交确认 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("练习提交确认邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("练习提交确认邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPracticeReviewEmail(String to, String username, String practiceTitle, String reviewStatus) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("practiceTitle", practiceTitle);
            context.setVariable("reviewStatus", reviewStatus);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/practice-review", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("练习评审通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("练习评审通知邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("练习评审通知邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendPracticeFeedbackEmail(String to, String username, String practiceTitle, String feedbackContent) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("practiceTitle", practiceTitle);
            context.setVariable("feedbackContent", feedbackContent);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/practice-feedback", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("练习反馈 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("练习反馈邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("练习反馈邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendWorkFeaturedEmail(String to, String username, String workTitle) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("workTitle", workTitle);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/work-featured", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("作品被推荐 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("作品推荐邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("作品推荐邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendWorkCommentEmail(String to, String username, String workTitle, String commenterName,
            String comment) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("workTitle", workTitle);
            context.setVariable("commenterName", commenterName);
            context.setVariable("comment", comment);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/work-comment", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("作品收到评论 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("作品评论邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("作品评论邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendWorkReportedEmail(String to, String moderatorName, String workTitle, String reporterName,
            String reason) {
        try {
            Context context = new Context();
            context.setVariable("moderatorName", moderatorName);
            context.setVariable("workTitle", workTitle);
            context.setVariable("reporterName", reporterName);
            context.setVariable("reason", reason);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/work-reported", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("作品被举报 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("作品举报邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("作品举报邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendFileRevisionRequestedEmail(String to, String username, String fileName, String revisionComment) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("fileName", fileName);
            context.setVariable("revisionComment", revisionComment);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-revision-requested", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("文件需要修支 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("文件修改请求邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("文件修改请求邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    @Async
    public void sendFileReviewAssignedEmail(String to, String reviewerName, String fileName, String uploaderName) {
        try {
            Context context = new Context();
            context.setVariable("reviewerName", reviewerName);
            context.setVariable("fileName", fileName);
            context.setVariable("uploaderName", uploaderName);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/file-review-assigned", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("文件审核分配 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("文件审核分配邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("文件审核分配邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName,
            Map<String, Object> templateVariables) {
        try {
            Context context = new Context();
            context.setVariables(templateVariables);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());
            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject + " - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("模板邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendEmailWithHtmlTemplate(String to, String subject, String htmlTemplate,
            Map<String, Object> templateVariables) {
        sendEmailWithTemplate(to, subject, htmlTemplate, templateVariables);
    }
    @Override
    public void sendEmailWithTextTemplate(String to, String subject, String textTemplate,
            Map<String, Object> templateVariables) {
        try {
            Context context = new Context();
            context.setVariables(templateVariables);
            String textContent = templateEngine.process(textTemplate, context);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject + " - " + appName);
            message.setText(textContent);
            message.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("文本模板邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendEmailAsync(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject + " - " + appName);
            message.setText(content);
            message.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("异步邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendEmailWithAttachment(String to, String subject, String content, String attachmentPath,
            String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject + " - " + appName);
            helper.setText(content, true);
            helper.setFrom(fromEmail);
            java.io.File file = new java.io.File(attachmentPath);
            if (file.exists()) {
                helper.addAttachment(attachmentName != null ? attachmentName : file.getName(), file);
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("附件邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendEmailWithMultipleAttachments(String to, String subject, String content,
            List<Map<String, String>> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject + " - " + appName);
            helper.setText(content, true);
            helper.setFrom(fromEmail);
            if (attachments != null) {
                for (Map<String, String> att : attachments) {
                    String path = att.get("path");
                    String name = att.getOrDefault("name", null);
                    if (path != null) {
                        java.io.File file = new java.io.File(path);
                        if (file.exists()) {
                            helper.addAttachment(name != null ? name : file.getName(), file);
                        }
                    }
                }
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("多附件邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public long getTotalEmailsBounced() {
        return 0;
    }
    @Override
    public boolean isEmailBlacklisted(String emailAddress) {
        return false;
    }
    @Override
    public void addToEmailBlacklist(String emailAddress) {
    }
    @Override
    public void removeFromEmailBlacklist(String emailAddress) {
    }
    @Override
    public boolean isEmailDomainValid(String domain) {
        return domain != null && domain.contains(".");
    }
    @Override
    public void updateEmailConfiguration(Map<String, Object> configuration) {
    }
    @Override
    public Map<String, Object> getEmailConfiguration() {
        return Map.of();
    }
    @Override
    public void sendPracticeAchievementEmail(String to, String username, String achievementName, String description) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("achievementName", achievementName);
            context.setVariable("description", description);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());
            String htmlContent = templateEngine.process("email/practice-achievement", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("练习成就 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("练习成就邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendPracticeMilestoneEmail(String to, String username, int milestoneNumber, String character) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("milestoneNumber", milestoneNumber);
            context.setVariable("character", character);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());
            String htmlContent = templateEngine.process("email/practice-milestone", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("练习里程碑 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("练习里程碑邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public void sendPracticeProgressReportEmail(String to, String username, int totalPractices, int completedPractices,
            double averageScore) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("totalPractices", totalPractices);
            context.setVariable("completedPractices", completedPractices);
            context.setVariable("averageScore", averageScore);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());
            String htmlContent = templateEngine.process("email/practice-progress", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("练习进度报告 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("练习进度报告邮件发送失败: {}", e.getMessage());
        }
    }
    @Override
    public boolean isEmailDelivered(String messageId) {
        return true;
    }
    @Override
    public void sendSystemAnnouncementEmail(String to, String username, String announcementTitle,
            String announcementContent) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("announcementTitle", announcementTitle);
            context.setVariable("announcementContent", announcementContent);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());
            String htmlContent = templateEngine.process("email/system-announcement", context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("系统公告 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("系统公告邮件发送失败: {}", e.getMessage());
        }
    }

    // Remove incorrect override annotations for non-interface methods
    @Async
    public void sendAchievementUnlockedEmail(String to, String username, String achievementName,
            String achievementDescription) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("achievementName", achievementName);
            context.setVariable("achievementDescription", achievementDescription);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/achievement-unlocked", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("成就解锁 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("成就解锁邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("成就解锁邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendLevelUpEmail(String to, String username, String currentLevel, String nextLevel,
            String requiredPoints) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("currentLevel", currentLevel);
            context.setVariable("nextLevel", nextLevel);
            context.setVariable("requiredPoints", requiredPoints);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/level-up", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("等级提升 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("等级提升邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("等级提升邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendPointEarnedEmail(String to, String username, String pointsEarned, String totalPoints) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("pointsEarned", pointsEarned);
            context.setVariable("totalPoints", totalPoints);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/point-earned", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("积分获得 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("积分获得邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("积分获得邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendBadgeEarnedEmail(String to, String username, String badgeName, String badgeDescription) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("badgeName", badgeName);
            context.setVariable("badgeDescription", badgeDescription);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/badge-earned", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("徽章获得 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("徽章获得邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("徽章获得邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestRegistrationEmail(String to, String username, String contestName, String contestDate) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("contestDate", contestDate);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-registration", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛报名确认 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛报名确认邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛报名确认邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestStartEmail(String to, String username, String contestName, String contestDuration) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("contestDuration", contestDuration);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-start", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛开始 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛开始邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛开始邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestEndEmail(String to, String username, String contestName, String finalRank) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("finalRank", finalRank);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-end", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛结束 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛结束邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛结束邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestWinnerEmail(String to, String username, String contestName, String prizeInfo) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("prizeInfo", prizeInfo);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-winner", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛获奖通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛获奖通知邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛获奖通知邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestResultEmail(String to, String username, String contestName, String contestResult,
            String contestScore) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("contestResult", contestResult);
            context.setVariable("contestScore", contestScore);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-result", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛结果 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛结果邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛结果邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestReminderEmail(String to, String username, String contestName, String contestDate,
            String contestTime) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("contestDate", contestDate);
            context.setVariable("contestTime", contestTime);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛提醒 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛提醒邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛提醒邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestInvitationEmail(String to, String username, String contestName, String inviterName) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("inviterName", inviterName);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-invitation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛邀请 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛邀请邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛邀请邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestCancellationEmail(String to, String username, String contestName,
            String cancellationReason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("cancellationReason", cancellationReason);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-cancellation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛取消通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛取消通知邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛取消通知邮件发送失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendContestUpdateEmail(String to, String username, String contestName, String updateContent) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("contestName", contestName);
            context.setVariable("updateContent", updateContent);
            context.setVariable("appName", appName);
            context.setVariable("currentYear", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("email/contest-update", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("比赛更新通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("比赛更新通知邮件发送成功: {}", to);

        } catch (MessagingException e) {
            log.error("比赛更新通知邮件发送失败: {}", e.getMessage());
        }
    }
}


