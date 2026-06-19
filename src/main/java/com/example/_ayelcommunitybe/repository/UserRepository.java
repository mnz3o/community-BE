package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Integer> {

    // 삭제되지 않은 회원 조회
    Optional<User> findByUserIdAndDeletedAtIsNull(
            int userId
    );

    // 이메일로 회원 조회
    Optional<User> findByEmail(
            String email
    );

    // 닉네임으로 회원 조회
    Optional<User> findByNickname(
            String nickname
    );

    // 이메일 중복 확인
    boolean existsByEmail(
            String email
    );

    // 닉네임 중복 확인
    boolean existsByNickname(
            String nickname
    );

    // 탈퇴하지 않은 회원만 이메일로 조회
    Optional<User> findByEmailAndDeletedAtIsNull(
            String email
    );

    // 탈퇴하지 않은 이메일 중복 확인
    boolean existsByEmailAndDeletedAtIsNull(
            String email
    );
}
