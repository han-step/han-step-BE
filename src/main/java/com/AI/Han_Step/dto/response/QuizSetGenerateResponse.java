package com.AI.Han_Step.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QuizSetGenerateResponse {

    private Long quizSetId;
    private String title;
    private int count;
}
