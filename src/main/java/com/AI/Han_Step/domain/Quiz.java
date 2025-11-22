package com.AI.Han_Step.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_set_id", nullable = false)
    private QuizSet quizSet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType type;  // SHORT_ANSWER or WORD_ORDER

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(nullable = false, length = 1000)
    private String answer;

    // WORD_ORDER나 객관식일 때 선택지 저장 (구분자는 나중에 프론트에서 split)
    @Column(length = 1000)
    private String choices;  // 예: "먹어요|고기를|강아지가|집에"
}

