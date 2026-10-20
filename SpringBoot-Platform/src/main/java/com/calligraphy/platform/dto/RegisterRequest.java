package com.calligraphy.platform.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "鐢ㄦ埛鍚嶄笉鑳戒负绌")
    @Size(min = 3, max = 50, message = "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-50涓瓧绗︿箣闂")
    private String username;
    
    @NotBlank(message = "閭涓嶈兘涓虹┖")
    @Email(message = "閭鏍煎紡涓嶆纭")
    @Size(max = 100, message = "閭长度不能超过100涓瓧绗")
    private String email;
    
    @NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 6, max = 100, message = "瀵嗙爜闀垮害蹇呴』鍦?-100涓瓧绗︿箣闂")
    private String password;
    
    @Size(max = 50, message = "鏄电О长度不能超过50涓瓧绗")
    private String nickname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}


