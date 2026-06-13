package com.example._ayelcommunitybe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // userId와 postId를 한 번에 초기화하기 위한 생성자
@EqualsAndHashCode
@Embeddable // 좋아요 복합키
public class PostLikeId implements Serializable {

    // 좋아요 복합키 구성 요소
    @Column(name = "user_id")
    private int userId;

    @Column(name = "post_id")
    private int postId;
}