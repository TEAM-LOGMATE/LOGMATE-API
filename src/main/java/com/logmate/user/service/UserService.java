package com.logmate.user.service;

import com.logmate.auth.util.JwtUtil;
import com.logmate.user.dto.UpdateUserRequest;
import com.logmate.user.model.User;
import com.logmate.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private void validatePassword(String password, String email) {
        // 최소 8자
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        // 영어, 숫자, 특수문자 조합
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).+$";
        if (!password.matches(regex)) {
            throw new IllegalArgumentException("비밀번호는 영어, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
        // 이메일과 동일한지 검사
        if (password.equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("비밀번호는 이메일과 동일할 수 없습니다.");
        }
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        //비밀번호 유효성 검증
        validatePassword(user.getPassword(), user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        System.out.println("입력된 비밀번호: " + password);
        System.out.println("DB 비밀번호: " + user.getPassword());

        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("비밀번호 불일치!");
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.generateToken(email);
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateUser(String currentEmail, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserRequest.getEmail())) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(updateUserRequest.getEmail());
        }

        if (updateUserRequest.getPassword() != null) {
            //비밀번호 유효성 검증
            validatePassword(updateUserRequest.getPassword(), user.getEmail());
            user.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmailIncludingInactive(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}
