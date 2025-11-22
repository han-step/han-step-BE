package com.AI.Han_Step.service;

import com.AI.Han_Step.domain.MemberProfile;
import com.AI.Han_Step.domain.QuizSet;
import com.AI.Han_Step.dto.request.MyInfoRequest;
import com.AI.Han_Step.dto.response.MyBriefingResponse;
import com.AI.Han_Step.dto.response.MyInfoResponse;
import com.AI.Han_Step.repository.MemberProfileRepository;
import com.AI.Han_Step.repository.QuizSetRepository;
import com.AI.Han_Step.service.ai.BriefingAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyInfoService {

    private final MemberProfileRepository memberProfileRepository;
    private final QuizSetRepository quizSetRepository;
    private final BriefingAiClient briefingAiClient;

    public MyInfoResponse saveMyInfo(MyInfoRequest request) {

        MemberProfile profile = memberProfileRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> MemberProfile.builder().build());

        profile.update(
                request.getName(),
                MemberProfile.KoreanLevel.valueOf(request.getKoreanLevel())
        );

        MemberProfile saved = memberProfileRepository.save(profile);

        return MyInfoResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .koreanLevel(saved.getKoreanLevel())
                .build();
    }

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo() {
        MemberProfile profile = memberProfileRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("등록된 내 정보가 없습니다. 먼저 등록해 주세요."));

        return MyInfoResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .koreanLevel(profile.getKoreanLevel())
                .build();
    }

    @Transactional(readOnly = true)
    public MyBriefingResponse getMyBriefing() {

        MemberProfile profile = memberProfileRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("등록된 내 정보가 없습니다. 먼저 등록해 주세요."));

        List<QuizSet> allQuizSets = quizSetRepository.findAll();

        long totalQuizSets = allQuizSets.size();
        long solvedQuizSets = allQuizSets.stream().filter(QuizSet::isSolved).count();
        long totalQuizzes = allQuizSets.stream()
                .mapToLong(qs -> qs.getTotalCount() == null ? 0 : qs.getTotalCount())
                .sum();

        double averageAccuracy = allQuizSets.stream()
                .filter(QuizSet::isSolved)
                .mapToDouble(QuizSet::getAccuracy)
                .average()
                .orElse(0.0);

        String briefing = briefingAiClient.generateBriefing(
                profile,
                totalQuizSets,
                solvedQuizSets,
                totalQuizzes,
                averageAccuracy
        );

        return MyBriefingResponse.builder()
                .briefing(briefing)
                .build();
    }
}
