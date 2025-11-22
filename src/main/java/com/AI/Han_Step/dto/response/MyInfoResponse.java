package com.AI.Han_Step.dto.response;

import com.AI.Han_Step.domain.MemberProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyInfoResponse {

    private Long id;
    private String name;
    private MemberProfile.KoreanLevel koreanLevel;

    private int solvedQuizCount;      // 내가 푼 퀴즈 총 개수
    private int solvedQuizSetCount;   // 내가 푼 퀴즈셋 개수

}
