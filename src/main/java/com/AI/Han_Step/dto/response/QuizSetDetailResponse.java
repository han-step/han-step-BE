package com.AI.Han_Step.dto.response;


import com.AI.Han_Step.domain.QuizSet;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class QuizSetDetailResponse {

    private Long id;
    private String title;
    private QuizSet.Level level;
    private List<QuizInSetResponse> quizzes;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class QuizInSetResponse {
        private Long id;
        private String type;      // SHORT_ANSWER / WORD_ORDER
        private String question;
        private String answer;    // WORD_ORDER면 "강아지가|고기를|먹어요"
        private String choices;   // choices "먹어요|고기를|강아지가|집에"
    }
}