package com.AI.Han_Step.service.ai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class QuizAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 한 세트 안에 CHOICE / WORD_ORDER 문제가 섞여서 나오도록 생성
     */
    public List<AiGeneratedQuiz> generateQuizzes(
            String level,
            int count
    ) {

        try {
            String prompt = buildPrompt(level, count);

            String requestBody =
                    """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "system",
                          "content": "You are a Korean language tutor that creates multiple-choice and word-order quizzes for multicultural Korean learners. You must output only valid JSON."
                        },
                        {
                          "role": "user",
                          "content": %s
                        }
                      ],
                      "temperature": 0.7
                    }
                    """.formatted(model, objectMapper.writeValueAsString(prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("STATUS = " + response.getStatusCode());
            System.out.println("BODY = " + response.getBody());

            return parseQuizzesFromResponse(response.getBody());

        } catch (HttpClientErrorException e) {
            System.out.println("=== OPENAI ERROR ===");
            System.out.println("STATUS: " + e.getStatusCode());
            System.out.println("ERROR BODY: " + e.getResponseBodyAsString());
            return List.of();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * level, count에 맞는 프롬프트 생성
     * - 다문화 가정용 한국어-only 퀴즈
     * - CHOICE / WORD_ORDER 두 가지 유형
     * - 영어 절대 사용 금지
     */
    private String buildPrompt(String level, int count) {
        return """
            한국어 기초 표현을 배우는 다문화 가정 학습자를 위한 한국어-only 퀴즈를 %d개 만들어줘.
            모든 문제와 보기(choices), 정답은 한국어만 사용해야 한다.
            영어 문장이나 영어 단어, 로마자 표기는 절대 사용하지 마라.

            문제 유형은 아래 두 가지가 섞이도록 만들어라. 난이도는 %s 수준에 맞게 조절해라.

            1) CHOICE (객관식)
               - 문제는 한국어 문장으로 질문한다.
               - 보기(choices)는 한국어 단어 또는 짧은 문장 4개이다.
               - 보기 중 1개만 정답이다.
               - 예시:
               {
                 "type": "CHOICE",
                 "question": "밥 먹기 전에 하는 말은?",
                 "choices": ["안녕", "잘가", "안돼", "잘 먹겠습니다"],
                 "answer": "잘 먹겠습니다"
               }

            2) WORD_ORDER (단어 배열)
               - 보기(choices)는 순서가 섞인 한국어 단어 배열이다.
               - 정답(answer)은 올바른 순서의 한국어 단어 배열이다.
               - 영어 번역은 절대 사용하지 않는다.
               - 예시:
               {
                 "type": "WORD_ORDER",
                 "question": "다음 단어를 올바르게 배열해 문장을 만드세요.",
                 "choices": ["읽어요", "책을", "그녀는"],
                 "answer": ["그녀는", "책을", "읽어요"]
               }

            출력 형식은 반드시 아래와 같은 JSON 배열로만 반환해라.
            여분의 설명, 텍스트, 번역은 절대 포함하지 마라.

            [
              {
                "type": "CHOICE or WORD_ORDER",
                "question": "질문 내용",
                "choices": ["보기1", "보기2", "보기3", "보기4"] 또는 ["단어1", "단어2", ...],
                "answer": "정답 텍스트" 또는 ["정답단어1","정답단어2",...]
              }
            ]
            """.formatted(count, level);
    }

    /**
     * OpenAI 응답에서 choices[0].message.content(JSON 문자열)를 꺼내서
     * List<AiGeneratedQuiz>로 변환
     */
    private List<AiGeneratedQuiz> parseQuizzesFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        JsonNode arrayNode = objectMapper.readTree(content);

        List<AiGeneratedQuiz> result = new ArrayList<>();

        for (JsonNode node : arrayNode) {
            String type = node.path("type").asText();          // "CHOICE" or "WORD_ORDER"
            String question = node.path("question").asText();

            // answer: CHOICE면 문자열, WORD_ORDER면 배열일 수도 있음
            String answer;
            JsonNode answerNode = node.path("answer");
            if (answerNode.isArray()) {
                // ["그녀는","책을","읽어요"] → "그녀는|책을|읽어요" 로 저장
                List<String> answerWords = new ArrayList<>();
                for (JsonNode a : answerNode) {
                    answerWords.add(a.asText());
                }
                answer = String.join("|", answerWords);
            } else {
                answer = answerNode.asText();
            }

            // choices
            JsonNode choicesNode = node.path("choices");
            List<String> choices = null;

            if (choicesNode.isArray()) {
                choices = new ArrayList<>();
                for (JsonNode c : choicesNode) {
                    choices.add(c.asText());
                }
            }

            result.add(new AiGeneratedQuiz(type, question, answer, choices));
        }

        return result;
    }
}
