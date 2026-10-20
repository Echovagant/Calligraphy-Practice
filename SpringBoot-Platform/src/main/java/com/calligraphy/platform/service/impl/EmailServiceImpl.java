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

    @Value("${app.name:涔︽硶缁冧範骞冲彴}")
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
            helper.setSubject("娉ㄥ唽纭 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("娉ㄥ唽纭閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("娉ㄥ唽纭閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("娉ㄥ唽娆㈣繋閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("娉ㄥ唽娆㈣繋閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("娉ㄥ唽鎴愬姛閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("娉ㄥ唽鎴愬姛閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("瀵嗙爜閲嶇疆閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("瀵嗙爜閲嶇疆閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("瀵嗙爜淇敼纭 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("瀵嗙爜淇敼纭閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("瀵嗙爜淇敼纭閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("瀵嗙爜閲嶇疆鎴愬姛閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("瀵嗙爜閲嶇疆鎴愬姛閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("璐︽埛宸查攣瀹?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("璐︽埛閿佸畾閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("璐︽埛閿佸畾閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("璐︽埛宸茶В閿?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("璐︽埛瑙ｉ攣閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("璐︽埛瑙ｉ攣閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鍙枒鐧诲綍灏濊瘯鎻愰啋 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鍙枒鐧诲綍灏濊瘯閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鍙枒鐧诲綍灏濊瘯閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂拌澶囩櫥褰曟彁閱?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鏂拌澶囩櫥褰曢偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂拌澶囩櫥褰曢偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            helper.setSubject("鍙岄噸楠岃瘉宸插惎鐢?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鍙岄噸楠岃瘉鍚敤閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鍙岄噸楠岃瘉鍚敤閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鍙岄噸楠岃瘉宸茬鐢?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鍙岄噸楠岃瘉绂佺敤閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鍙岄噸楠岃瘉绂佺敤閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("绠＄悊鍛樿处鎴峰垱寤?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("绠＄悊鍛樻敞鍐岄偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绠＄悊鍛樻敞鍐岄偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            helper.setSubject("绠＄悊鍛樿鑹插垎閰?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("绠＄悊鍛樿鑹插垎閰嶉偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绠＄悊鍛樿鑹插垎閰嶉偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            helper.setSubject("绠＄悊鍛樿鑹茬Щ闄?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("绠＄悊鍛樿鑹茬Щ闄ら偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绠＄悊鍛樿鑹茬Щ闄ら偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            helper.setSubject("绠＄悊鍛樿处鎴峰凡鍋滅敤 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("绠＄悊鍛樿处鎴峰仠鐢ㄩ偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绠＄悊鍛樿处鎴峰仠鐢ㄩ偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            helper.setSubject("绠＄悊鍛樿处鎴峰凡婵€娲?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("绠＄悊鍛樿处鎴锋縺娲婚偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绠＄悊鍛樿处鎴锋縺娲婚偖浠跺彂閫佸け璐? {}", e.getMessage());
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
                log.info("鎵归噺閭欢鍙戦€佹垚鍔? {}", to);
            }

        } catch (Exception e) {
            log.error("鎵归噺閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("鏂囦欢涓婁紶纭閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂囦欢涓婁紶纭閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂囦欢瀹℃牳寮€濮?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鏂囦欢瀹℃牳寮€濮嬮偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂囦欢瀹℃牳寮€濮嬮偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂囦欢瀹℃牳瀹屾垚 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鏂囦欢瀹℃牳瀹屾垚閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂囦欢瀹℃牳瀹屾垚閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂囦欢瀹℃牳缁撴灉閫氱煡 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鏂囦欢瀹℃牳閫氱煡閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂囦欢瀹℃牳閫氱煡閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("浣滃搧鍒涘缓閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("浣滃搧鍒涘缓閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("浣滃搧鏇存柊閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("浣滃搧鏇存柊閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("浣滃搧鍙戝竷閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("浣滃搧鍙戝竷閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("缁冧範鍒涘缓閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("缁冧範鍒涘缓閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("缁冧範鏇存柊閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("缁冧範鏇存柊閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("缁冧範鎻愰啋閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("缁冧範鎻愰啋閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("绯荤粺缁存姢閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绯荤粺缁存姢閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("绯荤粺閫氱煡閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绯荤粺閫氱煡閭欢鍙戦€佸け璐? {}", e.getMessage());
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

            log.info("鎵归噺閭欢鍙戦€佹垚鍔燂紝鍏卞彂閫?{} 灏侀偖浠", toList.size());
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException e) {
            log.error("鎵归噺閭欢鍙戦€佸け璐? {}", e.getMessage());
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
        log.info("娴嬭瘯閭欢閰嶇疆");
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
            // 绠€鍗曠殑閭欢鏈嶅姟鍙敤鎬ф鏌
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setTo(fromEmail);
            testMessage.setSubject("閭欢鏈嶅姟娴嬭瘯");
            testMessage.setText("閭欢鏈嶅姟娴嬭瘯");
            testMessage.setFrom(fromEmail);

            mailSender.send(testMessage);
            return true;
        } catch (Exception e) {
            log.error("閭欢鏈嶅姟涓嶅彲鐢? {}", e.getMessage());
            return false;
        }
    }
    @Override
    public long getEmailQueueSize() {
        // 杩欓噷搴旇瀹炵幇瀹為檯鐨勯槦鍒楀ぇ灏忔鏌
        return 0;
    }
    @Override
    public long getSentEmailCount() {
        // 杩欓噷搴旇瀹炵幇瀹為檯鐨勫彂閫佹暟閲忕粺璁
        return 0;
    }
    @Override
    public long getFailedEmailCount() {
        // 杩欓噷搴旇瀹炵幇瀹為檯鐨勫け璐ユ暟閲忕粺璁
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
        // 杩欓噷搴旇瀹炵幇閭欢閰嶇疆鏇存柊閫昏緫
        log.info("鏇存柊閭欢閰嶇疆: {}:{}", smtpHost, smtpPort);
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
            helper.setSubject("鏂囦欢瀹℃牳閫氳繃 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鏂囦欢瀹℃牳閫氳繃閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂囦欢瀹℃牳閫氳繃閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂囦欢瀹℃牳鏈€氳繃 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鏂囦欢瀹℃牳鏈€氳繃閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鏂囦欢瀹℃牳鏈€氳繃閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("缁冧範鎻愪氦纭 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("缁冧範鎻愪氦纭閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("缁冧範鎻愪氦纭閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("缁冧範璇勫通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("缁冧範璇勫閫氱煡閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("缁冧範璇勫閫氱煡閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("缁冧範鍙嶉 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("缁冧範鍙嶉閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("缁冧範鍙嶉閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("浣滃搧琚帹鑽?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("浣滃搧鎺ㄨ崘閭欢鍙戦€佹垚鍔? {}", to);
        } catch (MessagingException e) {
            log.error("浣滃搧鎺ㄨ崘閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("浣滃搧鏀跺埌璇勮 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("浣滃搧璇勮閭欢鍙戦€佹垚鍔? {}", to);
        } catch (MessagingException e) {
            log.error("浣滃搧璇勮閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("浣滃搧琚妇鎶?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("浣滃搧涓炬姤閭欢鍙戦€佹垚鍔? {}", to);
        } catch (MessagingException e) {
            log.error("浣滃搧涓炬姤閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂囦欢闇€瑕佷慨鏀?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("鏂囦欢淇敼璇锋眰閭欢鍙戦€佹垚鍔? {}", to);
        } catch (MessagingException e) {
            log.error("鏂囦欢淇敼璇锋眰閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鏂囦欢瀹℃牳鍒嗛厤 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
            log.info("鏂囦欢瀹℃牳鍒嗛厤閭欢鍙戦€佹垚鍔? {}", to);
        } catch (MessagingException e) {
            log.error("鏂囦欢瀹℃牳鍒嗛厤閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.error("妯℃澘閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.error("鏂囨湰妯℃澘閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.error("寮傛閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.error("闄勪欢閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.error("澶氶檮浠堕偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            log.error("缁冧範鎴愬氨閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("缁冧範閲岀▼纰?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("缁冧範閲岀▼纰戦偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            log.error("缁冧範杩涘害鎶ュ憡閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("绯荤粺鍏憡 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("绯荤粺鍏憡閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("鎴愬氨瑙ｉ攣 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("鎴愬氨瑙ｉ攣閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("鎴愬氨瑙ｉ攣閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("绛夌骇鎻愬崌閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绛夌骇鎻愬崌閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("绉垎鑾峰緱 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("绉垎鑾峰緱閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("绉垎鑾峰緱閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("寰界珷鑾峰緱閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("寰界珷鑾峰緱閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("姣旇禌鎶ュ悕纭 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("姣旇禌鎶ュ悕纭閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌鎶ュ悕纭閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("姣旇禌寮€濮?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("姣旇禌寮€濮嬮偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌寮€濮嬮偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            log.info("姣旇禌缁撴潫閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌缁撴潫閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("姣旇禌鑾峰通知 - " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("姣旇禌鑾峰閫氱煡閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌鑾峰閫氱煡閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("姣旇禌缁撴灉閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌缁撴灉閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("姣旇禌鎻愰啋閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌鎻愰啋閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            helper.setSubject("姣旇禌閭€璇?- " + appName);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("姣旇禌閭€璇烽偖浠跺彂閫佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌閭€璇烽偖浠跺彂閫佸け璐? {}", e.getMessage());
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
            log.info("姣旇禌鍙栨秷閫氱煡閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌鍙栨秷閫氱煡閭欢鍙戦€佸け璐? {}", e.getMessage());
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
            log.info("姣旇禌鏇存柊閫氱煡閭欢鍙戦€佹垚鍔? {}", to);

        } catch (MessagingException e) {
            log.error("姣旇禌鏇存柊閫氱煡閭欢鍙戦€佸け璐? {}", e.getMessage());
        }
    }
}


