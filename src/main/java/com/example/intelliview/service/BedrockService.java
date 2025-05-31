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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateInput;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateSessionConfiguration;
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

    public ArrayList<String> createProjectQuestions(Interview interview) throws IOException {
        Member member = interview.getMember();
        jsoupService.uploadToS3(interview.getGithubUsername(), member.getId());
        String basicQuery=  """
            You are an AI assistant tasked with generating technical interview questions for a computer engineering candidate.
            
            Your goal is to create 3 relevant and challenging questions tailored to the candidate’s specific occupation and their past projects, which are stored under the following S3 path: "{{S3_PATH}}". 
            This directory contains multiple subfolders for different repositories; explore them all as needed.
            
            All content must be written in Korean.
            
            Here is the candidate’s information:
            
            - Occupation: {{OCCUPATION}}
            
            Guidelines:
            
            1. Generate exactly 3 questions based on the candidate's past projects and occupation.
            2. Each question must include:
               - question: the question text (in Korean)
               - modelAnswer: a concise and accurate model answer (in Korean)
               - category: must be one of these 4 specific technical categories: BACKEND, FRONTEND, DEVOPS, CS
               - difficulty: an integer between 1 and 5 (where 5 is the most difficult)
            3. Use the contents of the candidate’s projects located in "{{S3_PATH}}" to derive question context (e.g., technologies used, project structure, design decisions).
            
            Output Constraint:
            
            - Your entire response **must be a JSON array only**.
            - **Do NOT include any planning, explanation, tags, or any text outside of the JSON array.**
            - Format the output as a proper JSON code block using triple backticks and `json`, like this:
            
               ```json
               [ ... your array of questions ... ]
               ```
               Example output format:
               [
                 {
                   "question": "DoctorSonju 프로젝트에서 시큐리티는 어떻게 구성했나요?",
                   "modelAnswer": "JWT 토큰을 사용해서 액세스 토큰과 리프레시 토큰을 레디스로 관리했습니다.",
                   "category": "BACKEND",
                   "difficulty": 2
                 },
                 {
                   "question": "CGCG 프로젝트에서 왜 MongoDB를 사용했나요?",
                   "modelAnswer": "채팅 기능이 있어서 read 연산의 속력을 높이고 싶어서 사용했습니다.",
                   "category": "DATABASE",
                   "difficulty": 3
                 },
                 {
                   "question": "CI/CD 파이프라인 구성 시 어떤 도구를 사용했고, 왜 선택했나요?",
                   "modelAnswer": "GitHub Actions와 Docker를 사용해서 자동 빌드와 배포를 구성했습니다.",
                   "category": "DEVOPS",
                   "difficulty": 4
                 }
               ]
               Remember: Only return a JSON array of 3 question objects. No explanation, no planning, no other text.
            """;
        String query = basicQuery.replace("{{OCCUPATION}}", interview.getOccupation());
        query = query.replace("{{S3_PATH}}", "repos/" + member.getId());
        String response = askAgent(query);
        String cleanJson = response.replaceAll("(?s)```json\\s*|\\s*```", "");
        ObjectMapper objectMapper = new ObjectMapper();
        List<GeneratedQuestionDto> questions = objectMapper.readValue(cleanJson, new TypeReference<>() {});
        ArrayList<String> questionList = new ArrayList<>();
        for (GeneratedQuestionDto generatedQuestion: questions) {
            Question question = Question.builder()
                .question(generatedQuestion.getQuestion())
                .category(generatedQuestion.getCategory())
                .difficulty(generatedQuestion.getDifficulty())
                .modelAnswer(generatedQuestion.getModelAnswer())
                .questionType(QuestionType.PROJECT)
                .build();
            questionRepository.save(question);
            questionList.add(generatedQuestion.getQuestion());
        }
        return(questionList);

    }

    public String askAgent(String userInput) {
        RetrieveAndGenerateRequest request = RetrieveAndGenerateRequest.builder()
            .sessionId(String.valueOf(UUID.randomUUID()))
            .input(RetrieveAndGenerateInput.builder()
                .text(userInput)
                .build())
            .retrieveAndGenerateConfiguration(RetrieveAndGenerateConfiguration.builder()
                .type("KNOWLEDGE_BASE")
                .knowledgeBaseConfiguration(KnowledgeBaseRetrieveAndGenerateConfiguration.builder()
                    .knowledgeBaseId(knowledgeBaseId)
                    .build())
                .build())
            .sessionConfiguration(RetrieveAndGenerateSessionConfiguration.builder()
                .build())
            .build();

        RetrieveAndGenerateResponse response = bedrockAgentRuntimeClient.retrieveAndGenerate(request);
        return response.output().text();
    }

}
