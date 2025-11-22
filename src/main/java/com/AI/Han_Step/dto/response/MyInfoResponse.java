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
}
