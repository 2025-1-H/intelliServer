package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.QuestionType;
import com.example.intelliview.dto.interview.GeneratedQuestionDto;
import com.example.intelliview.repository.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateInput;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BedrockService{

    @Value("${KNOWLEDGEBASE_ID}")
    private String knowledgeBaseId;

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final BedrockAgentRuntimeClient bedrockAgentRuntimeClient;
    private final QuestionRepository questionRepository;
    private final JSoupService jsoupService;


    public ArrayList<String> generateInterviewQuestions(Interview interview) throws JsonProcessingException {
        ArrayList<String> technicalQuestions = createTechnicalQuestions(interview);
        return technicalQuestions;
    }

    private ArrayList<String> createTechnicalQuestions(Interview interview) throws JsonProcessingException {
        JSONObject object = new JSONObject();
        String basicQuery = """
            You are an AI assistant tasked with generating technical interview questions for a computer engineering position.
            
            Your goal is to create 2 relevant and challenging questions tailored to the candidate’s specific occupation and qualifications.
            
            All content must be written in Korean.
            
            Here is the candidate’s information:
            
            - Occupation: {{OCCUPATION}}
            - Qualification: {{QUALIFICATION}}
            
            Guidelines:
            
            1. Generate exactly 2 questions related to different areas of computer engineering.
            2. Each question must include:
               - question: the question text (in Korean)
               - modelAnswer: a concise and accurate model answer (in Korean)
               - category: has to be one of these 4 specific technical category (BACKEND,FRONTEND,DEVOPS,CS)
               - difficulty: an integer between 1 and 5 (where 5 is the most difficult)
            3. The candidate’s qualifications should be reflected in the questions.
            
            Output Constraint:
            
            - Your entire response **must be a JSON array only**.
            - **Do NOT include any planning, explanation, tags (e.g., <question_planning>), or any text outside of the JSON array.**
            - Format the output as a proper JSON code block using triple backticks and `json` like this:
            
               ```json
               [ ... your array of questions ... ]
               ```
            Example output format:
            
            [
              {
                "question": "Explain the concept of database normalization and its benefits.",
                "modelAnswer": "Database normalization is the process of structuring a relational database to reduce data redundancy and improve data integrity...",
                "category": "Database",
                "difficulty": 3
              },
              {
                "question": "What is the difference between process and thread in operating systems?",
                "modelAnswer": "A process is an independent program with its own memory space...",
                "category": "CS Fundamentals",
                "difficulty": 2
              }
            ]
            
            Remember: only return a JSON array of 2 question objects. No explanation, no tags, no planning.
            """;
        String query = basicQuery.replace("{{OCCUPATION}}", interview.getOccupation());
        query = query.replace("{{QUALIFICATION}}", interview.getQualification());

        object.put("anthropic_version","bedrock-2023-05-31")
            .put("max_tokens", 2000)
            .put("temperature", 0.5);
        JSONObject message = new JSONObject().put("role", "user");
        JSONObject prompt = new JSONObject().put("type", "text").put("text", query);
        message = message.put("content", List.of(prompt));
        object = object.put("messages", List.of(message));
        String payload = object.toString();

        InvokeModelRequest request = InvokeModelRequest.builder()
            .modelId("anthropic.claude-3-5-sonnet-20240620-v1:0")
            .contentType("application/json")
            .accept("application/json")
            .body(SdkBytes.fromUtf8String(payload))
            .build();
        InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);
        String jsonBody = response.body().asUtf8String();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonBody);
        String rawText = rootNode.get("content").get(0).get("text").asText();
        String cleanJson = rawText.replaceAll("(?s)```json\\s*|\\s*```", "");
        List<GeneratedQuestionDto> questions = objectMapper.readValue(cleanJson, new TypeReference<>() {});
        ArrayList<String> questionList = new ArrayList<>();
        for (GeneratedQuestionDto generatedQuestion: questions) {
            Question question = Question.builder()
                .question(generatedQuestion.getQuestion())
                .category(generatedQuestion.getCategory())
                .difficulty(generatedQuestion.getDifficulty())
                .modelAnswer(generatedQuestion.getModelAnswer())
                .questionType(QuestionType.TECHNICAL)
                .build();
            questionRepository.save(question);
            questionList.add(generatedQuestion.getQuestion());
        }
        return(questionList);
    }

    public void createProjectQuestions(Interview interview) throws IOException {
        Member member = interview.getMember();
        jsoupService.uploadToS3(member.getGithubUsername(), member.getId());
        String basicQuery = """
            당신은 기술 면접관 역할을 맡고 있습니다.
            
            후보자는 "{{OCCUPATION}}"이며, 과거 프로젝트들은 Knowledge Base에 연결된 S3 경로 "{{S3_PATH}}"에 저장되어 있습니다.
            
            이 프로젝트들의 내용을 분석하여, 후보자의 기술 스택, 구조적 선택, 구현 방식 등을 바탕으로 면접 질문 5개를 생성해주세요.
            
            각 질문은 다음 정보를 포함해야 합니다:
            - question (질문 내용, 한국어)
            - modelAnswer (간단한 모범 답변, 한국어)
            - category: BACKEND, FRONTEND, DEVOPS, CS 중 하나
            - difficulty: 1~5 난이도 숫자
            
            질문은 후보자의 프로젝트 내용에 기반해야 하며, 일반적인 CS 질문은 가능한 피해주세요.
            출력 형식은 각 질문을 JSON 객체 배열로 만들어주세요. 이 외의 코멘트는 달지 말아주세요.
            """;
        String query = basicQuery.replace("{{OCCUPATION}}", interview.getOccupation());
        query = query.replace("{{S3_PATH}}", "repos/" + member.getId());
        String response = askAgent(query);
        log.info(response);
        ObjectMapper objectMapper = new ObjectMapper();
        List<GeneratedQuestionDto> questions;
        try {
            questions = objectMapper.readValue(response, new TypeReference<List<GeneratedQuestionDto>>() {});

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse questions from Bedrock response", e);
        }

        for (GeneratedQuestionDto generatedQuestion: questions) {
            Question question = Question.builder()
                .question(generatedQuestion.getQuestion())
                .category(generatedQuestion.getCategory())
                .difficulty(generatedQuestion.getDifficulty())
                .modelAnswer(generatedQuestion.getModelAnswer())
                .questionType(QuestionType.PROJECT)
                .build();
            questionRepository.save(question);
        }
    }

    public String askAgent(String userInput) {

        RetrieveAndGenerateRequest request = RetrieveAndGenerateRequest.builder()
            .input(RetrieveAndGenerateInput.builder()
                .text(userInput)
                .build())
            .retrieveAndGenerateConfiguration(RetrieveAndGenerateConfiguration.builder()
                .type("KNOWLEDGE_BASE")
                .knowledgeBaseConfiguration(KnowledgeBaseRetrieveAndGenerateConfiguration.builder()
                    .knowledgeBaseId(knowledgeBaseId)
                    .modelArn("arn:aws:bedrock:ap-northeast-2::foundation-model/anthropic.claude-3-5-sonnet-20240620-v1:0")
                    .retrievalConfiguration(KnowledgeBaseRetrievalConfiguration.builder()
                        .vectorSearchConfiguration(KnowledgeBaseVectorSearchConfiguration.builder()
                            .numberOfResults(15)
                            .build())
                        .build())
                    .build())
                .build())
            .build();


        try {
            RetrieveAndGenerateResponse response = bedrockAgentRuntimeClient.retrieveAndGenerate(request);
            return response.output().text();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
