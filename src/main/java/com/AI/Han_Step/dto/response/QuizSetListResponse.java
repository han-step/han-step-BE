package com.AI.Han_Step.dto.response;

import com.AI.Han_Step.domain.QuizSet;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class QuizSetListResponse {
    private Long id;
    private String title;
    private QuizSet.Level level;
    private long quizCount;  // 이 세트 안 문제 개수
    private boolean solved;
}
