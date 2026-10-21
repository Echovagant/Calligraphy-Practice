package com.calligraphy.platform.service;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.BackendUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final BackendUserRepository backendUserRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("UserDetailsServiceImpl.loadUserByUsername called with username: {}", username);

        // 先尝试从普通用户表查找
        java.util.Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!user.isEnabled()) {
                throw new UsernameNotFoundException("用户账户已被禁用: " + username);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(!user.isAccountNonLocked())
                    .credentialsExpired(false)
                    .disabled(!user.isEnabled())
                    .build();
        }

        // 再尝试从后端用户表查找
        java.util.Optional<BackendUser> backendUserOptional = backendUserRepository.findByUsername(username);
        if (backendUserOptional.isPresent()) {
            BackendUser backendUser = backendUserOptional.get();

            if (!backendUser.isAccountNonLocked()) {
                throw new UsernameNotFoundException("用户账户已被锁定: " + username);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + backendUser.getRole().name()));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(backendUser.getUsername())
                    .password(backendUser.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(!backendUser.isAccountNonLocked())
                    .credentialsExpired(false)
                    .disabled(false) // 后端用户没有enabled字段，默认为启用
                    .build();
        }

        throw new UsernameNotFoundException("用户不存在: " + username);
    }

    /**
     * 根据用户ID和用户类型加载用户详情      */
    public UserDetails loadUserByIdAndType(String userId, String userType) {
        log.info("UserDetailsServiceImpl.loadUserByIdAndType called with userId: {}, userType: {}", userId, userType);

        if ("user".equals(userType)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));

            if (!user.isEnabled()) {
                throw new UsernameNotFoundException("用户账户已被禁用: " + userId);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getId() + ":" + user.getRole().name() + ":" + user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(!user.isAccountNonLocked())
                    .credentialsExpired(false)
                    .disabled(!user.isEnabled())
                    .build();
        } else if ("admin".equals(userType)) {
            BackendUser backendUser = backendUserRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("管理员不存在: " + userId));

            if (!backendUser.isAccountNonLocked()) {
                throw new UsernameNotFoundException("管理员账户已被锁定: " + userId);
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + backendUser.getRole().name()));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(
                            backendUser.getId() + ":" + backendUser.getRole().name() + ":" + backendUser.getUsername())
                    .password(backendUser.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(!backendUser.isAccountNonLocked())
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }

        throw new UsernameNotFoundException("不支持的用户类型: " + userType);
    }
}


