package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
/*
 JPA는 객체 생성 시 기본 생성자가 필요하다.
 외부에서 직접 생성하는 것을 막기 위해 protected로 제한한다.
*/
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

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

    // 회원 생성
    public User(
            String email,
            String password,
            String nickname
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    // 닉네임 수정
    public void updateNickname(
            String nickname
    ) {
        this.nickname = nickname;
    }

    // 비밀번호 수정
    public void updatePassword(
            String password
    ) {
        this.password = password;
    }
}