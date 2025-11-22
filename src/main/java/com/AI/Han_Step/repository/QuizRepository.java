package com.AI.Han_Step.repository;

import com.AI.Han_Step.domain.Quiz;
import com.AI.Han_Step.domain.QuizSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    long countByQuizSet(QuizSet quizSet);

    List<Quiz> findByQuizSet(QuizSet quizSet);
}
