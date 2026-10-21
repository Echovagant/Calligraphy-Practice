package com.calligraphy.platform.service;

import java.util.List;

public interface EmailService {
    
    // 用户注册相关邮件
    void sendRegistrationConfirmationEmail(String to, String username, String confirmationToken);
    
    void sendRegistrationWelcomeEmail(String to, String username);
    
    void sendRegistrationSuccessEmail(String to, String username);
    
    // 密码重置相关邮件
    void sendPasswordResetEmail(String to, String username, String resetToken);
    
    void sendPasswordChangeConfirmationEmail(String to, String username);
    
    void sendPasswordResetSuccessEmail(String to, String username);
    
    // 账户安全相关邮件
    void sendAccountLockedEmail(String to, String username);
    
    void sendAccountUnlockedEmail(String to, String username);
    
    void sendSuspiciousLoginAttemptEmail(String to, String username, String ipAddress, String location);
    
    void sendLoginFromNewDeviceEmail(String to, String username, String deviceInfo, String ipAddress);
    
    void sendTwoFactorAuthEnabledEmail(String to, String username);
    
    void sendTwoFactorAuthDisabledEmail(String to, String username);
    
    // 管理员相关邮件 
    void sendAdminRegistrationEmail(String to, String username, String temporaryPassword);
    
    void sendAdminRoleAssignedEmail(String to, String username, String roleName);
    
    void sendAdminRoleRemovedEmail(String to, String username, String roleName);
    
    void sendAdminAccountDeactivatedEmail(String to, String username);
    
    void sendAdminAccountActivatedEmail(String to, String username);
    
    // 文件审核相关邮件
    void sendFileUploadConfirmationEmail(String to, String username, String fileName);
    
    void sendFileApprovedEmail(String to, String username, String fileName, String fileType);
    
    void sendFileRejectedEmail(String to, String username, String fileName, String fileType, String rejectReason);
    
    void sendFileRevisionRequestedEmail(String to, String username, String fileName, String revisionComment);
    
    void sendFileReviewAssignedEmail(String to, String reviewerName, String fileName, String uploaderName);
    
    // 作品相关邮件
    void sendWorkPublishedEmail(String to, String username, String workTitle);
    
    void sendWorkFeaturedEmail(String to, String username, String workTitle);
    
    void sendWorkCommentEmail(String to, String username, String workTitle, String commenterName, String comment);
    
    void sendWorkLikeEmail(String to, String username, String workTitle, String likerName);
    
    void sendWorkReportedEmail(String to, String moderatorName, String workTitle, String reporterName, String reason);
    
    // 练习相关邮件
    void sendPracticeReminderEmail(String to, String username, String practiceType, java.time.LocalDateTime scheduledTime);
    
    void sendPracticeAchievementEmail(String to, String username, String achievementName, String description);
    
    void sendPracticeMilestoneEmail(String to, String username, int milestoneNumber, String character);
    
    void sendPracticeProgressReportEmail(String to, String username, int totalPractices, int completedPractices, double averageScore);
    
    void sendPracticeSubmissionEmail(String to, String username, String practiceTitle, String submissionTime);
    
    void sendPracticeReviewEmail(String to, String username, String practiceTitle, String reviewStatus);
    
    void sendPracticeFeedbackEmail(String to, String username, String practiceTitle, String feedbackContent);
    
    // 系统通知邮件
    void sendSystemMaintenanceEmail(String to, String maintenanceType, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    void sendSystemUpdateEmail(String to, String username, String updateDescription);
    
    void sendSystemAnnouncementEmail(String to, String username, String announcementTitle, String announcementContent);
    
    void sendPrivacyPolicyUpdateEmail(String to, String username);
    
    void sendTermsOfServiceUpdateEmail(String to, String username);
    
    void sendSystemNotificationEmail(String to, String subject, String content, java.util.Map<String, Object> templateData);
    
    // 批量邮件
    void sendBulkEmail(List<String> toList, String subject, String content);
    
    void sendBulkEmailWithTemplate(List<String> toList, String templateName, java.util.Map<String, Object> templateVariables);
    
    java.util.concurrent.CompletableFuture<Boolean> sendBulkEmail(List<String> toList, String subject, String content, java.util.Map<String, Object> templateData);
    
    // 邮件模板管理
    void sendEmailWithTemplate(String to, String subject, String templateName, java.util.Map<String, Object> templateVariables);
    
    void sendEmailWithHtmlTemplate(String to, String subject, String htmlTemplate, java.util.Map<String, Object> templateVariables);
    
    void sendEmailWithTextTemplate(String to, String subject, String textTemplate, java.util.Map<String, Object> templateVariables);
    
    // 邮件队列管理
    void sendEmailAsync(String to, String subject, String content);
    
    void sendEmailWithAttachment(String to, String subject, String content, String attachmentPath, String attachmentName);
    
    void sendEmailWithMultipleAttachments(String to, String subject, String content, java.util.List<java.util.Map<String, String>> attachments);
    
    // 邮件状态跟踪 
    boolean isEmailSent(String messageId);
    boolean isEmailDelivered(String messageId);
    boolean isEmailOpened(String messageId);
    boolean isEmailClicked(String messageId);
    java.util.Map<String, Object> getEmailStatus(String messageId);
    
    // 邮件统计
    long getTotalEmailsSent();
    
    long getTotalEmailsDelivered();
    
    long getTotalEmailsOpened();
    
    long getTotalEmailsClicked();
    
    long getTotalEmailsBounced();
    
    double getEmailDeliveryRate();
    
    double getEmailOpenRate();
    
    double getEmailClickRate();
    
    double getEmailBounceRate();
    
    long getEmailQueueSize();
    
    long getSentEmailCount();
    
    long getFailedEmailCount();
    
    double getEmailSuccessRate();
    
    // 邮件配置
    void updateEmailConfiguration(String smtpHost, int smtpPort, String username, String password);
    
    void updateEmailConfiguration(java.util.Map<String, Object> configuration);
    
    java.util.Map<String, Object> getEmailConfiguration();
    
    void testEmailConfiguration();
    
    boolean isEmailServiceAvailable();
    
    // 邮件验证
    boolean validateEmailAddress(String emailAddress);
    boolean isEmailBlacklisted(String emailAddress);
    void addToEmailBlacklist(String emailAddress);
    void removeFromEmailBlacklist(String emailAddress);
    boolean isEmailDomainValid(String domain);
}


