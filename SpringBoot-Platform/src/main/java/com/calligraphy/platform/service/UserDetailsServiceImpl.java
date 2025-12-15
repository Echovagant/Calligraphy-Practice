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

        // 鍏堝皾璇曚粠鏅€氱敤鎴疯〃鏌ユ壘
        java.util.Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!user.isEnabled()) {
                throw new UsernameNotFoundException("鐢ㄦ埛璐︽埛宸茶绂佺敤: " + username);
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

        // 鍐嶅皾璇曚粠鍚庣鐢ㄦ埛琛ㄦ煡鎵?        java.util.Optional<BackendUser> backendUserOptional = backendUserRepository.findByUsername(username);
        if (backendUserOptional.isPresent()) {
            BackendUser backendUser = backendUserOptional.get();

            if (!backendUser.isAccountNonLocked()) {
                throw new UsernameNotFoundException("鐢ㄦ埛璐︽埛宸茶閿佸畾: " + username);
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
                    .disabled(false) // 鍚庣鐢ㄦ埛娌℃湁enabled瀛楁锛岄粯璁や负鍚敤
                    .build();
        }

        throw new UsernameNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + username);
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鍜岀敤鎴风被鍨嬪姞杞界敤鎴疯鎯?     */
    public UserDetails loadUserByIdAndType(String userId, String userType) {
        log.info("UserDetailsServiceImpl.loadUserByIdAndType called with userId: {}, userType: {}", userId, userType);

        if ("user".equals(userType)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));

            if (!user.isEnabled()) {
                throw new UsernameNotFoundException("鐢ㄦ埛璐︽埛宸茶绂佺敤: " + userId);
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
                    .orElseThrow(() -> new UsernameNotFoundException("绠＄悊鍛樹笉瀛樺湪: " + userId));

            if (!backendUser.isAccountNonLocked()) {
                throw new UsernameNotFoundException("绠＄悊鍛樿处鎴峰凡琚攣瀹? " + userId);
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

        throw new UsernameNotFoundException("涓嶆敮鎸佺殑鐢ㄦ埛绫诲瀷: " + userType);
    }
}


