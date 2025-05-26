package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.Question;
import com.example.intelliview.dto.interview.GeneratedQuestionDto;
import com.example.intelliview.repository.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BedrockService{
    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final QuestionRepository questionRepository;

    public ArrayList<String> generateInterviewQuestions(Interview interview) throws JsonProcessingException {
        JSONObject object = new JSONObject();
        String basicQuery = """
            You are an AI assistant tasked with generating technical interview questions for a computer engineering position.
            
            Your goal is to create 5 relevant and challenging questions tailored to the candidate’s specific occupation and qualifications.
            
            All content must be written in Korean.
            
            Here is the candidate’s information:
            
            - Occupation: {{OCCUPATION}}
            - Qualification: {{QUALIFICATION}}
            
            Guidelines:
            
            1. Generate exactly 5 questions related to different areas of computer engineering.
            2. Each question must include:
               - question: the question text (in Korean)
               - modelAnswer: a concise and accurate model answer (in Korean)
               - category: has to be one of these 4 specific technical category (BACKEND,FRONTEND,DEVOPS,CS)
               - difficulty: an integer between 1 and 5 (where 5 is the most difficult)
            3. The difficulty distribution must be:
               - 1 question with difficulty 2
               - 2 questions with difficulty 3
               - 2 questions with difficulty 4
            4. The candidate’s qualifications should be reflected in the questions.
            
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
            
            Remember: only return a JSON array of 5 question objects. No explanation, no tags, no planning.
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
        System.out.println(cleanJson);
        List<GeneratedQuestionDto> questions = objectMapper.readValue(cleanJson, new TypeReference<>() {});
        ArrayList<String> questionList = new ArrayList<>();
        for (GeneratedQuestionDto generatedQuestion: questions) {
            Question question = Question.builder()
                .question(generatedQuestion.getQuestion())
                .category(generatedQuestion.getCategory())
                .difficulty(generatedQuestion.getDifficulty())
                .modelAnswer(generatedQuestion.getModelAnswer())
                .build();
            questionRepository.save(question);
            questionList.add(generatedQuestion.getQuestion());
        }
        return(questionList);
    }
}
