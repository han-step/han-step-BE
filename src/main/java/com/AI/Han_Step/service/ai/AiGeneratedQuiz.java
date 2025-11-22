package com.AI.Han_Step.service.ai;

import java.util.List;

public record AiGeneratedQuiz(
        String type,              // "SHORT_ANSWER" or "WORD_ORDER"
        String question,
        String answer,
        List<String> choices      // null or 빈 리스트일 수 있음
) {}
