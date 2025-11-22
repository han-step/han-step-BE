package com.AI.Han_Step.service.ai;

import com.AI.Han_Step.domain.MemberProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class BriefingAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 내 정보 + 퀴즈 통계를 기반으로 AI 브리핑 텍스트 생성
     */
    public String generateBriefing(MemberProfile profile,
                                   long totalQuizSets,
                                   long solvedQuizSets,
                                   long totalQuizzes,
                                   double averageAccuracy) {

        try {
            String prompt = buildPrompt(profile, totalQuizSets, solvedQuizSets, totalQuizzes, averageAccuracy);

            String requestBody =
                    """
                    {
                      "model": "%s",
                      "messages": [
                        {
                          "role": "system",
                          "content": "You are a kind Korean language tutor who explains study feedback in simple Korean. Always answer in Korean only."
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

            String body = response.getBody();
            JsonNode root = objectMapper.readTree(body);
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return content;

        } catch (HttpClientErrorException e) {
            System.out.println("=== OPENAI BRIEFING ERROR ===");
            System.out.println("STATUS: " + e.getStatusCode());
            System.out.println("ERROR BODY: " + e.getResponseBodyAsString());
            return "AI 브리핑을 생성하는 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.";

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 브리핑을 생성하는 중 오류가 발생했어요. 잠시 후 다시 시도해 주세요.";
        }
    }

    /**
     * 프롬프트 생성 (이름 + 레벨 + 퀴즈 통계만 사용)
     */
    private String buildPrompt(MemberProfile profile,
                               long totalQuizSets,
                               long solvedQuizSets,
                               long totalQuizzes,
                               double averageAccuracy) {

        long accuracyPercent = Math.round(averageAccuracy * 100);

        return """
            아래 학습자의 정보를 기반으로 간단한 한국어 학습 브리핑을 작성해줘.

            [학습자 정보]
            - 이름: %s
            - 한국어 레벨: %s

            [학습 통계]
            - 전체 퀴즈 세트 수: %d
            - 푼 세트 수: %d
            - 전체 푼 문제 수(통계용): %d
            - 평균 정답률: %d%%

            조건:
            1) 반드시 한국어로만 작성할 것.
            2) 4~6문장 정도로 짧고 따뜻하게 코멘트할 것.
            3) 현재 학습 상황을 칭찬해주고, 앞으로 어떤 방향으로 공부하면 좋을지 간단히 제안해라.
            4) 너무 어려운 한국어 표현은 피하고, 다문화 가정 학습자가 이해하기 쉽게 써라.
            """.formatted(
                profile.getName(),
                profile.getKoreanLevel().name(),
                totalQuizSets,
                solvedQuizSets,
                totalQuizzes,
                accuracyPercent
        );
    }
}
