package com.example.intelliview.controller;

import com.example.intelliview.dto.user.JoinDTO;
import com.example.intelliview.service.JoinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {

    private final JoinService joinService;

    private JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/api/v1/auth/signup")
    public String joinProcess(JoinDTO joinDTO) {

        joinService.joinProcess(joinDTO);
        return "success";
    }
}