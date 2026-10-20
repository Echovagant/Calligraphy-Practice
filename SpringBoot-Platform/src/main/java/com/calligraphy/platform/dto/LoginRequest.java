package com.calligraphy.platform.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class LoginRequest {
    
    @NotBlank(message = "鐢ㄦ埛鍚嶄笉鑳戒负绌")
    @Size(min = 3, max = 50, message = "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-50涓瓧绗︿箣闂")
    private String username;
    
    @NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 6, max = 100, message = "瀵嗙爜闀垮害蹇呴』鍦?-100涓瓧绗︿箣闂")
    private String password;

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
}


