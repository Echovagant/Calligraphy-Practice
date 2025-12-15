package com.calligraphy.platform.service;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.dto.LoginRequestDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.RegisterRequestDto;
import com.calligraphy.platform.dto.RegisterResponseDto;

import java.util.Map;

public interface AuthService {

    Map<String, String> login(String username, String password);
    Map<String, String> refreshTokenMap(String refreshToken);
    User register(User user);
    
    LoginResponseDto login(LoginRequestDto loginRequest);
    RegisterResponseDto register(RegisterRequestDto registerRequest);
    LoginResponseDto refreshToken(String refreshToken);
    void logout(String token);
    void logout(String token, String refreshToken);
}


