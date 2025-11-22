package com.AI.Han_Step.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizSubmitResponse {

    private Long quizSetId;
    private int totalCount;
    private int correctCount;
    private double accuracy;
    private long elapsedMillis;
}
