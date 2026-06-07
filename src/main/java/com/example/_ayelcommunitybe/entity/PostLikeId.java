package com.example._ayelcommunitybe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 복합키 객체 생성 시userId와 postId를 한 번에 초기화하기 위해 생성자를 제공함
@EqualsAndHashCode
@Embeddable // 복합키를 별도 객체로 분리하여 관리
public class PostLikeId implements Serializable {

    // 좋아요 복합키 구성 요소
    @Column(name = "user_id")
    private int userId;

    @Column(name = "post_id")
    private int postId;
}