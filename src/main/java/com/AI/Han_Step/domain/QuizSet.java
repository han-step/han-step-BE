package com.AI.Han_Step.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_set")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class QuizSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    // ❗ 프론트에서 이 세트를 풀었는지 여부
    @Column(nullable = false)
    private boolean solved;

    // ❗ 프론트가 계산해서 보내는 값 저장
    private Integer totalCount;
    private Integer correctCount;
    private Long elapsedMillis;

    public enum Level {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // 🔹 세트를 완료 처리하는 메서드
    public void solve(int totalCount, int correctCount, long elapsedMillis) {
        this.solved = true;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.elapsedMillis = elapsedMillis;
    }

    // 🔹 정답률 계산(프론트에서 보내도 되지만, 서버서도 사용 가능)
    public double getAccuracy() {
        if (totalCount == null || totalCount == 0) return 0.0;
        return (double) correctCount / totalCount;
    }
}
