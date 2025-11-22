package com.AI.Han_Step.service;

import com.AI.Han_Step.domain.Quiz;
import com.AI.Han_Step.domain.QuizSet;
import com.AI.Han_Step.domain.QuizType;
import com.AI.Han_Step.dto.request.QuizSetGenerateRequest;
import com.AI.Han_Step.dto.request.QuizSubmitRequest;
import com.AI.Han_Step.dto.response.QuizSetDetailResponse;
import com.AI.Han_Step.dto.response.QuizSetGenerateResponse;
import com.AI.Han_Step.dto.response.QuizSetListResponse;
import com.AI.Han_Step.dto.response.QuizSubmitResponse;
import com.AI.Han_Step.repository.QuizRepository;
import com.AI.Han_Step.repository.QuizSetRepository;
import com.AI.Han_Step.service.ai.AiGeneratedQuiz;
import com.AI.Han_Step.service.ai.QuizAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizSetService {

    private final QuizSetRepository quizSetRepository;
    private final QuizRepository quizRepository;
    private final QuizAiClient quizAiClient;

    /**
     * AI로 퀴즈 세트를 생성하고 DB에 QuizSet + Quiz 저장
     */
    public QuizSetGenerateResponse generateQuizSetByAi(QuizSetGenerateRequest request) {

        // 1) QuizSet 저장
        QuizSet quizSet = QuizSet.builder()
                .title(request.getTitle())
                .level(QuizSet.Level.valueOf(request.getLevel()))
                .build();

        quizSetRepository.save(quizSet);

        // 2) AI에게 문제 생성 요청
        List<AiGeneratedQuiz> aiQuizzes =
                quizAiClient.generateQuizzes(
                        request.getLevel(),
                        request.getCount()
                );

        // 3) 생성된 문제 저장
        for (AiGeneratedQuiz g : aiQuizzes) {

            // 문제 유형 (SHORT_ANSWER, WORD_ORDER)
            QuizType type = QuizType.valueOf(g.type());

            // choices: null 또는 "먹어요|고기를|강아지가"
            String choicesStr = null;
            if (g.choices() != null && !g.choices().isEmpty()) {
                choicesStr = String.join("|", g.choices());
            }

            Quiz quiz = Quiz.builder()
                    .quizSet(quizSet)
                    .type(type)
                    .question(g.question())
                    .answer(g.answer())   // 단어 배열형이면 "강아지가|고기를|먹어요" 형태
                    .choices(choicesStr)
                    .build();

            quizRepository.save(quiz);
        }

        // 4) 응답 반환
        return QuizSetGenerateResponse.builder()
                .quizSetId(quizSet.getId())
                .title(quizSet.getTitle())
                .count(aiQuizzes.size())
                .build();
    }

    public List<QuizSetListResponse> getQuizSetList() {

        List<QuizSet> quizSets = quizSetRepository.findAll();

        return quizSets.stream()
                .map(qs -> QuizSetListResponse.builder()
                        .id(qs.getId())
                        .title(qs.getTitle())
                        .level(qs.getLevel())
                        .quizCount(quizRepository.countByQuizSet(qs))  // 문제 개수
                        .solved(qs.isSolved())
                        .build())
                .toList();
    }

    public QuizSetDetailResponse getQuizSetDetail(Long quizSetId) {

        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new IllegalArgumentException("QuizSet not found: " + quizSetId));

        List<Quiz> quizzes = quizRepository.findByQuizSet(quizSet);

        List<QuizSetDetailResponse.QuizInSetResponse> quizResponses =
                quizzes.stream()
                        .map(q -> QuizSetDetailResponse.QuizInSetResponse.builder()
                                .id(q.getId())
                                .type(q.getType().name())   // SHORT_ANSWER, WORD_ORDER
                                .question(q.getQuestion())
                                .answer(q.getAnswer())
                                .choices(q.getChoices())     // "먹어요|고기를|..." or null
                                .build()
                        )
                        .toList();

        return QuizSetDetailResponse.builder()
                .id(quizSet.getId())
                .title(quizSet.getTitle())
                .level(quizSet.getLevel())
                .quizzes(quizResponses)
                .build();
    }

    public QuizSubmitResponse submitQuizSet(Long quizSetId, QuizSubmitRequest request) {

        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new IllegalArgumentException("QuizSet not found: " + quizSetId));

        // 프론트 계산 → 서버는 저장만
        quizSet.solve(
                request.getTotalCount(),
                request.getCorrectCount(),
                request.getElapsedMillis()
        );

        return QuizSubmitResponse.builder()
                .quizSetId(quizSet.getId())
                .totalCount(quizSet.getTotalCount())
                .correctCount(quizSet.getCorrectCount())
                .accuracy(quizSet.getAccuracy())
                .elapsedMillis(quizSet.getElapsedMillis())
                .build();
    }

    public void autoGenerateQuizSet() {

        long unsolvedCount = quizSetRepository.countBySolvedFalse();

        // 3개 이상이면 생성 안 함
        if (unsolvedCount >= 3) {
            System.out.println("[자동 생성] 현재 '안 푼' QuizSet 개수 = " + unsolvedCount + " → 생성 스킵");
            return;
        }

        // 레벨 enum 실제 값에 맞춰서 수정!
        QuizSet.Level defaultLevel = QuizSet.Level.BEGINNER;

        // 랜덤 타이틀 생성
        String title = generateRandomTitle();

        QuizSetGenerateRequest request = QuizSetGenerateRequest.builder()
                .title(title)
                .level(defaultLevel.name())
                .count(6)
                .build();

        System.out.println("[자동 생성] 새 QuizSet 생성 시도 → title=" + title);

        generateQuizSetByAi(request);
    }


    private String generateRandomTitle() {
        int idx = (int) (Math.random() * RANDOM_TITLES.size());
        return RANDOM_TITLES.get(idx);
    }

    private static final List<String> RANDOM_TITLES = List.of(
            "오늘의 한국어 도전",
            "쉬운 한국어 퀴즈",
            "기본 문장 익히기",
            "단어 연습 세트",
            "왕초보 문장 만들기",
            "한글 감 잡기",
            "간단한 표현 배우기",
            "기초 회화 연습",
            "문장 순서 맞추기",
            "단어 조합 연습",
            "한국어 첫걸음",
            "간단 문장 도전",
            "문장 만들기 연습",
            "데일리 한국어 퀴즈",
            "한국어 교정 연습",
            "핵심 단어 익히기",
            "오늘의 표현",
            "문장 구성 훈련",
            "한국어 기초 완성",
            "짧은 문장 연습"
    );

}
