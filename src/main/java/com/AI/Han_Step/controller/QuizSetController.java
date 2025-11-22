package com.AI.Han_Step.controller;


import com.AI.Han_Step.dto.request.QuizSetGenerateRequest;
import com.AI.Han_Step.dto.request.QuizSubmitRequest;
import com.AI.Han_Step.dto.response.QuizSetDetailResponse;
import com.AI.Han_Step.dto.response.QuizSetGenerateResponse;
import com.AI.Han_Step.dto.response.QuizSetListResponse;
import com.AI.Han_Step.dto.response.QuizSubmitResponse;
import com.AI.Han_Step.service.QuizSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz-sets")
public class QuizSetController {

    private final QuizSetService quizSetService;

    // + 버튼 눌렀을 때 프론트에서 호출할 API
    @PostMapping("/ai-generate")
    public QuizSetGenerateResponse generateByAi(@RequestBody QuizSetGenerateRequest request) {
        return quizSetService.generateQuizSetByAi(request);
    }

    // 세트 목록 조회
    @GetMapping
    public List<QuizSetListResponse> getQuizSetList() {
        return quizSetService.getQuizSetList();
    }

    // 세트 상세 + 문제들 조회
    @GetMapping("/{id}")
    public QuizSetDetailResponse getQuizSetDetail(@PathVariable Long id) {
        return quizSetService.getQuizSetDetail(id);
    }

    @PostMapping("/{id}/submit")
    public QuizSubmitResponse submitQuizSet(
            @PathVariable Long id,
            @RequestBody QuizSubmitRequest request
    ) {
        return quizSetService.submitQuizSet(id, request);
    }
}

