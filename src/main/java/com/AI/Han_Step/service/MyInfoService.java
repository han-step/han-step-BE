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

        QuizStats stats = calculateQuizStats();

        return MyInfoResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .koreanLevel(saved.getKoreanLevel())
                .solvedQuizCount(stats.solvedQuizCount())
                .solvedQuizSetCount(stats.solvedQuizSetCount())
                .build();
    }

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo() {
        MemberProfile profile = memberProfileRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("등록된 내 정보가 없습니다. 먼저 등록해 주세요."));

        QuizStats stats = calculateQuizStats();

        return MyInfoResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .koreanLevel(profile.getKoreanLevel())
                .solvedQuizCount(stats.solvedQuizCount())
                .solvedQuizSetCount(stats.solvedQuizSetCount())
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
    @Transactional
    public MyInfoResponse updateMyInfo(MyInfoRequest request) {

        MemberProfile profile = memberProfileRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("등록된 내 정보가 없습니다. 먼저 등록해 주세요."));

        // 기존 profile 값 업데이트
        profile.update(
                request.getName(),
                MemberProfile.KoreanLevel.valueOf(request.getKoreanLevel())
        );

        // JPA dirty checking 으로 자동 업데이트
        QuizStats stats = calculateQuizStats();

        return MyInfoResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .koreanLevel(profile.getKoreanLevel())
                .solvedQuizCount(stats.solvedQuizCount())
                .solvedQuizSetCount(stats.solvedQuizSetCount())
                .build();
    }


    /**
     * 내가 푼 퀴즈/퀴즈셋 통계 계산
     * - solvedQuizSetCount : isSolved == true 인 QuizSet 개수
     * - solvedQuizCount    : isSolved == true 인 QuizSet 들의 totalCount 합
     */
    private QuizStats calculateQuizStats() {
        List<QuizSet> allQuizSets = quizSetRepository.findAll();

        long solvedQuizSetCountLong = allQuizSets.stream()
                .filter(QuizSet::isSolved)
                .count();

        long solvedQuizCountLong = allQuizSets.stream()
                .filter(QuizSet::isSolved)
                .mapToLong(qs -> qs.getTotalCount() == null ? 0 : qs.getTotalCount())
                .sum();

        // 여기서 int로 캐스팅 (실제 값이 int 범위 안이라고 가정)
        int solvedQuizSetCount = (int) solvedQuizSetCountLong;
        int solvedQuizCount = (int) solvedQuizCountLong;

        return new QuizStats(solvedQuizCount, solvedQuizSetCount);
    }

    // DTO와 맞게 int 타입으로 record 정의
    private record QuizStats(int solvedQuizCount, int solvedQuizSetCount) {}
}

