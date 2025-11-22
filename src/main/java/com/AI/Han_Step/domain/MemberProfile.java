package com.AI.Han_Step.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 항상 1개만 사용할 예정

    @Column(nullable = false)
    private String name;   // 학습자 이름 또는 닉네임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KoreanLevel koreanLevel;

    public enum KoreanLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    public void update(String name, KoreanLevel koreanLevel) {
        this.name = name;
        this.koreanLevel = koreanLevel;
    }
}
