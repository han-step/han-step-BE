package com.AI.Han_Step.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSetGenerateRequest {
    private String title;   // 세트 이름
    private String level;   // "BEGINNER" / "INTERMEDIATE" / "ADVANCED"
    private int count;      // 문제 개수
}