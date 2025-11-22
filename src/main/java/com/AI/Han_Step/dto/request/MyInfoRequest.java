package com.AI.Han_Step.dto.request;

import lombok.Getter;

@Getter
public class MyInfoRequest {

    private String name;
    private String koreanLevel;   // "BEGINNER", "INTERMEDIATE", "ADVANCED"
}
