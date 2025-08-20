package com.logmate.user.controller;

import com.logmate.auth.dto.LoginRequest;
import com.logmate.auth.dto.LoginResponse;
import com.logmate.auth.util.JwtUtil;
import com.logmate.auth.util.TokenBlacklist;
import com.logmate.user.dto.UpdateUserRequest;
import com.logmate.user.service.UserService;
import com.logmate.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenBlacklist tokenBlacklist;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User saved = userService.register(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.add(token);
            return ResponseEntity.ok("로그아웃 완료");
        }

        return ResponseEntity.badRequest().body("토큰 없음");
    }

    @GetMapping("/mypage")
    public ResponseEntity<?> getMyPage(HttpServletRequest request) {
        String email = jwtUtil.extractEmailFromRequest(request);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/mypage")
    public ResponseEntity<?> updateMyProfile(@RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
        String currentEmail = jwtUtil.extractEmailFromRequest(httpRequest);
        userService.updateUser(currentEmail, request);
        return ResponseEntity.ok("수정 완료");
    }

    //TODO soft delete 실행 유무 협의
    @DeleteMapping("/mypage")
    public ResponseEntity<?> deleteMyAccount(HttpServletRequest httpRequest) {
        String email = jwtUtil.extractEmailFromRequest(httpRequest);
        userService.deleteUser(email);
        return ResponseEntity.ok("계정 탈퇴 와료");
    }
}