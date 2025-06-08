package com.example.intelliview.controller;

import com.example.intelliview.dto.user.JoinDTO;
import com.example.intelliview.dto.user.LoginDTO;
import com.example.intelliview.service.JoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
public class AuthController {

    private final JoinService joinService;

    private AuthController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/api/v1/auth/signup")
    public ResponseEntity<String> joinProcess(@RequestBody JoinDTO joinDTO) {
        joinService.joinProcess(joinDTO);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        // Swagger 노출용. 실제 로그인은 SecurityFilterChain에서 처리
        return ResponseEntity.ok("로그인 요청이 전달되었습니다. (실제 처리는 필터에서 수행됨)");
    }
}