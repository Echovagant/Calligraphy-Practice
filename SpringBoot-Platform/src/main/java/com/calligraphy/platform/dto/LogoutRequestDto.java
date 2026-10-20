package com.calligraphy.platform.dto;

public class LogoutRequestDto {
    
    private String refreshToken;
    
    private String deviceInfo;
    
    private String ipAddress;
    
    // 鏃犲弬鏋勯€犲櫒
    public LogoutRequestDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
    public LogoutRequestDto(String refreshToken, String deviceInfo, String ipAddress) {
        this.refreshToken = refreshToken;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
    }
    
    // Getter 鍜?Setter 方法
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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
    
    // Builder 绫
    public static class Builder {
        private String refreshToken;
        private String deviceInfo;
        private String ipAddress;
        
        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public Builder deviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public LogoutRequestDto build() {
            return new LogoutRequestDto(refreshToken, deviceInfo, ipAddress);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


