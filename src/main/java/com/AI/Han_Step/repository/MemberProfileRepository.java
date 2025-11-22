package com.AI.Han_Step.repository;

import com.AI.Han_Step.domain.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

    // 로그인/회원 개념이 없으므로 항상 "첫 번째 프로필 하나"만 사용
    Optional<MemberProfile> findTopByOrderByIdAsc();
}
