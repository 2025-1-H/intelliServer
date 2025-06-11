package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.InterviewReport;
import com.example.intelliview.domain.Question;
import com.example.intelliview.dto.FeedbackResponse;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.QuestionType;
import com.example.intelliview.dto.interview.GeneratedQuestionDto;
import com.example.intelliview.dto.interview.InterviewReportDto;
import com.example.intelliview.dto.interview.UploadedVideoDto;
import com.example.intelliview.repository.InterviewReportRepository;
import com.example.intelliview.repository.InterviewRepository;
import com.example.intelliview.repository.QuestionRepository;
import com.example.intelliview.service.S3Uploader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrievalConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseRetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.KnowledgeBaseVectorSearchConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateConfiguration;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateInput;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
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
    private final BedrockRuntimeClient bedrockNovaRuntimeClient;
    private final QuestionRepository questionRepository;
    private final InterviewReportRepository interviewReportRepository;
    private final InterviewRepository interviewRepository;
    private final S3Uploader s3Uploader;

    @Value("${AWS_ACCOUNT_ID}")
    private String awsAccountId;
    private final JSoupService jsoupService;

    public ArrayList<Question> generateInterviewQuestions(Interview interview) throws JsonProcessingException {
        ArrayList<Question> technicalQuestions = createTechnicalQuestions(interview);
        return technicalQuestions;
    }

    private ArrayList<Question> createTechnicalQuestions(Interview interview) throws JsonProcessingException {
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
        ArrayList<Question> questionList = new ArrayList<>();
        for (GeneratedQuestionDto generatedQuestion: questions) {
            Question question = Question.builder()
                    .question(generatedQuestion.getQuestion())
                    .category(generatedQuestion.getCategory())
                    .difficulty(generatedQuestion.getDifficulty())
                    .modelAnswer(generatedQuestion.getModelAnswer())
                    .questionType(QuestionType.TECHNICAL)
                    .build();
            questionRepository.save(question);
            questionList.add(question);
        }
        return(questionList);
    }

    public String uploadToS3AndAnalyzeInterview(MultipartFile videoFile, Long interviewId) {
        try {
            // 1. S3 업로드
            UploadedVideoDto uploaded = s3Uploader.upload(videoFile);
            String key = uploaded.getKey();
            String s3Uri = uploaded.getS3Url();

            // 2. 시스템 메시지 (선택적)
            JSONArray systemArray = new JSONArray();
            systemArray.put(new JSONObject().put("text", "당신은 전문가 면접 분석가입니다."));

            // 3. 사용자 메시지
            JSONArray contentArray = new JSONArray();
            contentArray.put(new JSONObject()
                    .put("video", new JSONObject()
                    .put("format", "webm")
                    .put("source", new JSONObject()
                    .put("s3Location", new JSONObject()
                    .put("uri", s3Uri)
                    .put("bucketOwner", awsAccountId)
                                )
                            )
                    )
            );
            contentArray.put(new JSONObject()
                    .put("text",
                    """
                    당신은 10년 이상의 경험을 가진 기술 면접 평가 전문가입니다.
                    컴퓨터공학 및 웹 개발·인프라 분야의 실무 역량과 커뮤니케이션 스킬을 종합적으로 평가하는 전문성을 보유하고 있습니다.
                    제공되는 면접 영상은 컴퓨터공학 전공자를 대상으로 한 웹 개발, 인프라 구성, 프로젝트 경험 등 실무 기반 질문에 대한 응답을 담고 있습니다.
                    먼저 영상에서 제시된 기술적 질문들을 파악하고 요약한 후, 다음 항목에 대해 1-5점 척도로 평가하고 각 항목마다 구체적인 한국어 피드백 3문장 이상을 작성해주세요:

                    1. 면접 태도:
                    - 아이컨택: 카메라 응시의 자연스러움과 일관성
                    - 손동작: 설명을 돕는 적절한 제스처 활용도
                    - 표정: 자신감과 진정성이 드러나는 표현
                    - 자세: 안정적이고 전문적인 포스처 유지
                    - 긴장도: 과도한 경직성이나 산만함 여부

                    2. 음성 표현:
                    - 톤의 적절성: 상황에 맞는 음성 톤 사용
                    - 발음 명료성: 전달하고자 하는 내용의 정확한 발음
                    - 말하기 속도: 듣기 편한 적정 속도 유지
                    - 강조와 억양: 핵심 포인트의 효과적인 강조
                    - 음성 안정성: 떨림이나 불안정함 없는 일관된 음성

                    3. 반언어적 표현:
                    - 말의 리듬감: 자연스러운 화법과 적절한 쉼
                    - 간투사 빈도: "음", "어", "그" 등 불필요한 간투사 사용 정도
                    - 침묵 처리: 생각할 시간이 필요할 때의 자연스러운 대응
                    - 감정 표현: 내용에 따른 적절한 감정적 뉘앙스
                    - 흐름의 연속성: 끊김 없는 자연스러운 대화 진행

                    4. 내용 구성:
                    - 논리적 구조: 서론-본론-결론의 명확한 흐름
                    - 질문 이해도: 면접관 질문의 핵심 의도 파악 정도
                    - 구체성: 실제 경험이나 프로젝트 사례의 적절한 활용
                    - 완결성: 답변의 완성도와 충분한 설명 제공
                    - 연관성: 질문과 답변 간의 직접적 연결성

                    5. 지식 전달력:
                    - 전문 지식 정확성: 기술적 개념과 용어의 정확한 사용
                    - 설명 능력: 복잡한 기술 내용의 이해하기 쉬운 전달
                    - 실무 연결성: 이론과 실무 경험의 적절한 연계
                    - 문제 해결 사고: 기술적 문제에 대한 논리적 접근법
                    - 지식의 깊이: 표면적이지 않은 심화된 이해도 표현

                    평가 척도:
                    - 5점: 매우 우수 (전문가 수준의 뛰어난 역량)
                    - 4점: 우수 (기대 수준을 상회하는 좋은 역량)
                    - 3점: 보통 (기본적인 요구 사항을 충족하는 수준)
                    - 2점: 부족 (개선이 필요한 수준)
                    - 1점: 매우 부족 (상당한 개선이 필요한 수준)

                    다음 HTML 형식으로 출력해주세요. 그 외의 설명이나 코멘트는 절대 포함하지 마세요:

                            <div class="interview-evaluation">
                             <div class="section">
                               <h2>📋 질문 분석 및 응답 평가</h2>
                               <div class="questions">
                                 <div class="question-item">
                                   <h3>질문 1</h3>
                                   <p><strong>질문 요약:</strong> [질문 내용 요약]</p>
                                   <p><strong>응답 평가:</strong> [응답의 적절성 및 기술적 정확성 평가]</p>
                                   <p><strong>개선 제안:</strong> [구체적인 개선 방안]</p>
                                 </div>
                                 <div class="question-item">
                                   <h3>질문 2</h3>
                                   <p><strong>질문 요약:</strong> [질문 내용 요약]</p>
                                   <p><strong>응답 평가:</strong> [응답의 적절성 및 기술적 정확성 평가]</p>
                                   <p><strong>개선 제안:</strong> [구체적인 개선 방안]</p>
                                 </div>
                                 <div class="question-item">
                                   <h3>질문 3</h3>
                                   <p><strong>질문 요약:</strong> [질문 내용 요약]</p>
                                   <p><strong>응답 평가:</strong> [응답의 적절성 및 기술적 정확성 평가]</p>
                                   <p><strong>개선 제안:</strong> [구체적인 개선 방안]</p>
                                 </div>
                               </div>
                             </div>
                             <div class="section">
                               <h2>🎯 세부 평가 항목</h2>
                               <div class="evaluation-item">
                                 <h3>1. 면접 태도</h3>
                                 <div class="score">점수: <span class="score-value">[1-5점]</span>/5</div>
                                 <div class="feedback">[구체적인 피드백 - 관찰된 행동과 개선점을 포함하여 3문장 이상]</div>
                               </div>
                               <div class="evaluation-item">
                                 <h3>2. 음성 표현</h3>
                                 <div class="score">점수: <span class="score-value">[1-5점]</span>/5</div>
                                 <div class="feedback">[구체적인 피드백 - 음성의 장단점과 개선 방향을 제시하여 3문장 이상]</div>
                               </div>
                               <div class="evaluation-item">
                                 <h3>3. 반언어적 표현</h3>
                                 <div class="score">점수: <span class="score-value">[1-5점]</span>/5</div>
                                 <div class="feedback">[구체적인 피드백 - 말하기 습관과 자연스러움을 평가하여 3문장 이상]</div>
                               </div>
                               <div class="evaluation-item">
                                 <h3>4. 내용 구성</h3>
                                 <div class="score">점수: <span class="score-value">[1-5점]</span>/5</div>
                                 <div class="feedback">[구체적인 피드백 - 답변 구조와 논리성에 대한 평가를 3문장 이상]</div>
                               </div>
                               <div class="evaluation-item">
                                 <h3>5. 지식 전달력</h3>
                                 <div class="score">점수: <span class="score-value">[1-5점]</span>/5</div>
                                 <div class="feedback">[구체적인 피드백 - 기술적 역량과 설명 능력을 평가하여 3문장 이상]</div>
                               </div>
                             </div>
                             <div class="section">
                               <h2>📊 종합 평가</h2>
                               <div class="total-score">
                                 <h3>총점: <span class="total-score-value">[총점]</span>/100</h3>
                               </div>
                               <div class="summary-section">
                                 <div class="well-done">
                                   <h4>✅ 잘한 점</h4>
                                   <ul>
                                     <li>[구체적인 강점 1]</li>
                                     <li>[구체적인 강점 2]</li>
                                     <li>[구체적인 강점 3]</li>
                                   </ul>
                                 </div>
                                 <div class="to-improve">
                                   <h4>🔧 개선할 점</h4>
                                   <ul>
                                     <li>[구체적인 개선점 1]</li>
                                     <li>[구체적인 개선점 2]</li>
                                     <li>[구체적인 개선점 3]</li>
                                   </ul>
                                 </div>
                               </div>
                               <div class="overall-summary">
                                 <h4>💡 종합 의견</h4>
                                 <p>[전반적인 면접 역량 평가 및 발전 방향 제시 - 150-200자 내외]</p>
                               </div>
                             </div>
                            </div>
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   평가 시 주의사항:
                    1. 객관적이고 건설적인 피드백 제공
                    2. 구체적인 관찰 내용을 바탕으로 한 평가
                    3. 개선 가능한 실행 방안 포함
                    4. 면접자의 잠재력과 성장 가능성 고려
                    5. 문화적, 개인적 특성에 대한 편견 배제
                    6. 질문과 답변의 연관성을 중점적으로 분석
                    7. 기술적 질문에 대한 정확성과 깊이 있는 이해도 평가
                    """
            ));

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", contentArray));

            JSONObject inferenceConfig = new JSONObject()
                    .put("maxTokens", 10000)
                    .put("temperature", 0.3)
                    .put("topP", 0.9)
                    .put("topK", 20);

            JSONObject requestPayload = new JSONObject()
                    .put("schemaVersion", "messages-v1")
                    .put("system", systemArray)
                    .put("messages", messages)
                    .put("inferenceConfig", inferenceConfig);

            // 4. Bedrock 호출
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("amazon.nova-lite-v1:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestPayload.toString()))
                    .build();


            InvokeModelResponse response = bedrockNovaRuntimeClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();

            // 5. 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            String text = root.get("output").get("message").get("content").get(0).get("text").asText();

            // 6. DB 저장
            Interview interview = interviewRepository.findById(interviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Interview not found"));

            InterviewReport report = InterviewReport.builder()
                    .interview(interview)
                    .content(text)
                    .videoUrl(s3Uri)
                    .build();
            interviewReportRepository.save(report);

            return text;

        } catch (SdkServiceException sse) {
            log.error("Bedrock service exception (HTTP {}) : {}", sse.statusCode(), sse.getMessage(), sse);
            throw new RuntimeException("Bedrock service 에러: " + sse.getMessage(), sse);

        } catch (SdkClientException sce) {
            log.error("Bedrock client exception: {}", sce.getMessage(), sce);
            throw new RuntimeException("Bedrock client 에러: " + sce.getMessage(), sce);

        } catch (Exception e) {
            log.error("면접 분석 실패", e);
            throw new RuntimeException("Interview video analysis failed");
        }
    }

    public void createProjectQuestions(Interview interview) throws IOException {
        Member member = interview.getMember();
        //jsoupService.uploadToS3(member.getGithubUsername(), member.getId());
        String basicQuery = """
            당신은 기술 면접관 역할을 맡고 있습니다.
            
            후보자는 "{{OCCUPATION}}"이며, 과거 프로젝트들은 Knowledge Base에 연결된 S3 경로 "{{S3_PATH}}"에 저장되어 있습니다.
            
            이 프로젝트들의 내용을 분석하여, 후보자의 기술 스택, 구조적 선택, 구현 방식 등을 바탕으로 면접 질문 5개를 생성해주세요.
            
            각 질문은 다음 정보를 포함해야 합니다:
            - question (질문 내용, 한국어)
            - modelAnswer (간단한 모범 답변, 한국어)
            - category: BACKEND, FRONTEND, DEVOPS, CS 중 하나
            - difficulty: 1~5 난이도 숫자
            
            질문은 후보자의 프로젝트 내용에 기반해야합니다.
            출력 형식은 각 질문을 JSON 객체 배열로 만들어주세요. 이 외의 코멘트는 달지 말아주세요.
            안된다고 하지말고 어떻게든 결과물을 만들어주세요.
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
