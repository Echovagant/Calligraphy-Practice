package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "前端用户更新请求DTO")
public class FrontendUserUpdateDto {

    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "用户昵称")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别", example = "male")
    private String gender;

    @Schema(description = "生日", example = "1990-01-01")
    private java.time.LocalDate birthday;

    @Schema(description = "个人简介", example = "这是一个用户简介")
    private String bio;

    @Schema(description = "账号级偏好，JSON 字符串。null 表示不改动")
    private String preferences;

    @Schema(description = "状态", example = "active")
    private String status;

    // 无参构造器
    public FrontendUserUpdateDto() {
    }

    // 全参构造器
    public FrontendUserUpdateDto(Long userId, String nickname, String email, String phone, String avatar,
                                String gender, java.time.LocalDate birthday, String bio, String status,
                                String preferences) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
        this.gender = gender;
        this.birthday = birthday;
        this.bio = bio;
        this.status = status;
        this.preferences = preferences;
    }
    
    // Getter 和 Setter 方法
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public java.time.LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(java.time.LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    // Builder 类
    public static class Builder {
    private Long userId;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String gender;
    private java.time.LocalDate birthday;
    private String bio;
    private String status;
    private String preferences;

    public Builder userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Builder nickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Builder email(String email) {
        this.email = email;
        return this;
    }

    public Builder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public Builder avatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Builder gender(String gender) {
        this.gender = gender;
        return this;
    }

    public Builder birthday(java.time.LocalDate birthday) {
        this.birthday = birthday;
        return this;
    }

    public Builder bio(String bio) {
        this.bio = bio;
        return this;
    }

    public Builder status(String status) {
        this.status = status;
        return this;
    }

    public Builder preferences(String preferences) {
        this.preferences = preferences;
        return this;
    }

    public FrontendUserUpdateDto build() {
        return new FrontendUserUpdateDto(userId, nickname, email, phone, avatar, gender, birthday, bio, status,
                preferences);
    }

    }

    public static Builder builder() {
        return new Builder();
    }
}
