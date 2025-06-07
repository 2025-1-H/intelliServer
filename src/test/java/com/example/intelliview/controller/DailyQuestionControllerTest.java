//package com.example.intelliview.controller;
//
//import com.example.intelliview.domain.Member;
//import com.example.intelliview.dto.DailyQuestionResponse;
//import com.example.intelliview.service.DailyQuestionService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//
//
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.RequestPostProcessor;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@AutoConfigureMockMvc
//@ContextConfiguration(classes = DailyQuestionController.class)
//@WebAppConfiguration
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//class DailyQuestionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private DailyQuestionService dailyQuestionService;
//
//    @Test
//    void getTodayQuestion_shouldReturnOk() throws Exception {
//        // given
//        DailyQuestionResponse dummyResponse = new DailyQuestionResponse(1L, "오늘의 질문은 무엇인가요");
//        Member mockMember = Member.builder()
//                .id(1L)
//                .username("username")
//                .password("password")
//                .email("test@email.com")
//                .build();
//
//        //PrincipalDetails principalDetails = new PrincipalDetails(memberRepository.findById(1L).get());
//
//        when(dailyQuestionService.getTodayQuestion(any()))
//                .thenReturn(dummyResponse);
//
//        // when + then
//        mockMvc.perform(get("/api/v1/daily/question")
//                        .with(user("jake").roles("USER"))
//                        .contentType(MediaType.APPLICATION_JSON)) // 인증된 사용자 시뮬레이션
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.questionId").value(1L))
//                .andExpect(jsonPath("$.questionText").value("오늘의 질문은 무엇인가요?"));
//    }
//}