package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDto {
    
    @NotBlank(message = "鐢ㄦ埛鍚嶄笉鑳戒负绌")
    @Size(min = 3, max = 50, message = "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-50浣嶄箣闂")
    private String username;
    
    @NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 6, max = 100, message = "瀵嗙爜闀垮害蹇呴』鍦?-100浣嶄箣闂")
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


