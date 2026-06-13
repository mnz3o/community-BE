package com.example._ayelcommunitybe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호를 평문으로 저장하지 않고 BCrypt 해시로 암호화
        return new BCryptPasswordEncoder();
    }
}
