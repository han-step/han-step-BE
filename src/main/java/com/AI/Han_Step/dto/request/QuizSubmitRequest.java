package com.AI.Han_Step.dto.request;

import lombok.Getter;

@Getter
public class QuizSubmitRequest {

    private int totalCount;       // 전체 문제 수
    private int correctCount;     // 맞춘 문제 수
    private long elapsedMillis;   // 소요 시간(ms)
}
