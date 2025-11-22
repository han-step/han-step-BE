package com.AI.Han_Step.scheduler;

import com.AI.Han_Step.service.QuizSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizSetScheduler {

    private final QuizSetService quizSetService;

    /**
     * 1시간마다 실행
     * cron = 초 분 시 일 월 요일
     * 0 0 * * * ? → 매 정각(00분)에 실행
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void generateQuizSetEveryHour() {

        System.out.println("[스케줄러] 1시간마다 자동 생성 트리거 실행");

        quizSetService.autoGenerateQuizSet();
    }
}

