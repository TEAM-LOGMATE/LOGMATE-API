package com.logmate.config;

import com.logmate.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    /*@Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 서버)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").permitAll() // 회원가입/로그인 허용
                        .anyRequest().authenticated()                 // 그 외는 인증 필요
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults()); // 테스트용 basic 로그인 허용

        return http.build();
    }*/
    // 프론트 연동을 위해 임시로 로그인 X
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                .cors(Customizer.withDefaults()) // CORS 활성화
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 서버)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()                   // 전체 허용
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable())    // 기본 폼 로그인 끄기
                .httpBasic(basic -> basic.disable()); // 기본 BasicAuth 끄기

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}