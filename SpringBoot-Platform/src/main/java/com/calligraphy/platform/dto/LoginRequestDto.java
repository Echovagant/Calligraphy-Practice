package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50位之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100位之间")
    private String password;
    
    private String twoFactorCode;
    private boolean rememberMe;
    private String deviceInfo;
    private String ipAddress;
    
    public LoginRequestDto() {
    }
    
    public LoginRequestDto(String username, String password, String twoFactorCode, boolean rememberMe, String deviceInfo, String ipAddress) {
        this.username = username;
        this.password = password;
        this.twoFactorCode = twoFactorCode;
        this.rememberMe = rememberMe;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTwoFactorCode() {
        return twoFactorCode;
    }
    
    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}


