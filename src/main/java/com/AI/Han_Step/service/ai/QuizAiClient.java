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

        // 비율: 높임말 1/3, 의미상황 1/3, 나머지 WORD_ORDER
        int honorificCount = Math.max(1, count / 3);
        int meaningCount = Math.max(1, count / 3);
        int wordOrderCount = count - honorificCount - meaningCount;

        return """
        한국어 기초 표현을 배우는 다문화 가정 학습자를 위한 한국어-only 퀴즈를 총 %d개 만들어줘.
        모든 문제와 보기(choices), 정답은 한국어만 사용해야 한다.
        영어 문장, 영어 단어, 로마자 표기는 절대 사용하지 마라.

        난이도는 %s 수준에 맞게 조절해라.

        ────────────────────────────────────────
        [전체 구성 규칙]
        ────────────────────────────────────────
        - 전체 문제 수는 정확히 %d개여야 한다.
        - 이 중 정확히 %d개는 "반말→높임말" CHOICE 문제여야 한다.
        - 정확히 %d개는 "상황·의미에 알맞은 표현" CHOICE 문제여야 한다.
        - 정확히 %d개는 WORD_ORDER(단어 배열) 문제여야 한다.
        - 세 종류 문제는 지정된 개수를 반드시 맞춰라.

        ────────────────────────────────────────
        [문제 중복 금지]
        ────────────────────────────────────────
        - 한 세트 내부에서 question / choices / answer는 모두 서로 완전히 달라야 한다.
        - 동일한 표현을 살짝만 바꿔 재사용하지 마라.
        - 비슷한 의미라도 문장 구조가 동일하면 중복으로 간주한다.

        ────────────────────────────────────────
        [공통 CHOICE 규칙]
        ────────────────────────────────────────
        - question(질문)에는 정답 표현을 절대 그대로 쓰지 마라. (상황만 설명)
        - CHOICE 문제는 보기 중 오직 1개만 정답이어야 한다.
        - question과 answer는 의미적으로 완전히 일치해야 한다.
        - 같은 의미를 가진 표현(예: "미안해"와 "죄송합니다")은
          question에서 “반말/존댓말” 기준을 명시하지 않으면 choices에 함께 넣지 마라.

        ────────────────────────────────────────
        [표현 의미 규칙 (필수)]
        ────────────────────────────────────────
        * “안녕하세요” : 처음 만났을 때, 아침에 일어나서, 낮에 인사할 때
        * “안녕히 주무세요” : 잠자리에 들기 전 어른께 드리는 인사
        * “감사합니다” : 고마운 일을 예의 있게 표현할 때
        * “죄송합니다” : 어른/선생님/낯선 사람에게 공손하게 사과할 때
        * “미안해” : 친구/가족 등에게 가볍게 사과하는 표현
        
        보기에 비슷한 답과 비슷한 보기를 만들지 마라
        예) 답:감사합니다 일때 보기에 고마워와 같이 의미가 같은말을 쓰지마라

        예)
        - “아침에 잠에서 깨어날 때 하는 인사” → 정답: “안녕하세요”
        - “잠자리에 들기 전 어른께 하는 인사” → 정답: “안녕히 주무세요”
        - “선생님께 공손하게 사과할 때” → 정답: “죄송합니다”
        - “친구에게 가볍게 사과할 때” → 정답: “미안해”

        ────────────────────────────────────────
        [1) 반말 → 높임말 CHOICE 문제 (%d개)]
        ────────────────────────────────────────
        - type = "CHOICE"
        - 아래 반말을 상황으로 제시하고, choices에는 높임말이 포함되도록 한다.

          - 고마워  → 감사합니다
          - 안녕    → 안녕하세요
          - 잘자    → 안녕히 주무세요
          - 미안해  → 죄송합니다

        - question 예시:
          "친구에게는 '고마워'라고 말하지만, 선생님께는 무엇이라고 말해야 하나요?"

        ────────────────────────────────────────
        [2) 의미·상황 CHOICE 문제 (%d개)]
        ────────────────────────────────────────
        - type = "CHOICE"
        - 일상 상황을 설명하고, 가장 적절한 표현을 고르는 문제를 만든다.
        - question은 상황 설명만 포함하고, 표현 자체를 넣지 않는다.

        예:
        {
          "type": "CHOICE",
          "question": "아침에 처음 만난 사람에게 하는 인사는?",
          "choices": ["안녕히 주무세요", "안녕하세요", "미안해", "고마워"],
          "answer": "안녕하세요"
        }

        ────────────────────────────────────────
        [3) WORD_ORDER 문제 (%d개)]
        ────────────────────────────────────────
        - type = "WORD_ORDER"
        - question은 반드시 상황 설명 또는 안내 문장으로 구성한다.
        - answer는 반드시 한국어 단어 배열(JSON 배열)로 제공한다.
        - choices는 반드시 answer 배열의 동일한 단어들을 순서만 섞어서 구성해야 한다.
          - 단어를 추가/삭제/변경하면 안 된다.
          - 예)
            answer: ["그녀는", "책을", "읽어요"]
            choices: ["책을", "읽어요", "그녀는"]   ← 순서만 섞기

        ────────────────────────────────────────
        [출력 형식 (절대 어기지 마라)]
        ────────────────────────────────────────
        오직 아래 형식의 JSON 배열만 반환해야 한다:

        [
          {
            "type": "CHOICE" 또는 "WORD_ORDER",
            "question": "상황 설명",
            "choices": ["보기1", "보기2", ...] 또는 ["단어1", "단어2", ...],
            "answer": "정답 문장" 또는 ["정답단어1","정답단어2",...]
          }
        ]

        추가 설명, 말머리, 번역, 마크다운, 불필요한 텍스트는 절대 포함하지 마라.
        """.formatted(
                count,
                level,
                count,
                honorificCount,
                meaningCount,
                wordOrderCount,
                honorificCount,
                meaningCount,
                wordOrderCount
        );
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
