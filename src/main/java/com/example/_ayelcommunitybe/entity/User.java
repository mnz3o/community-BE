package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter

/*
 JPA는 객체 생성 시 기본 생성자가 필요하다.

 외부에서 직접 생성하는 것을 막기 위해 protected로 제한한다.
*/
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    // 이메일 중복 X
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // 닉네임 중복 X
    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 회원 생성
    public User(
            String email,
            String password,
            String nickname
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 닉네임 수정
    public void updateNickname(
            String nickname
    ) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    // 비밀번호 수정
    public void updatePassword(
            String password
    ) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    // 회원 탈퇴
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}