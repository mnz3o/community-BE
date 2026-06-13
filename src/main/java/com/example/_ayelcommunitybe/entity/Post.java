package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private int postId;

    @ManyToOne(fetch = FetchType.LAZY) // 게시글(N)은 하나의 사용자(1)가 작성 가능
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post") // 게시글(1)은 여러 파일(N)을 가질 수 있음
    private List<StoredFile> files = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "comment_count")
    private int commentCount;

    // 게시글 생성
    public Post(
            User user,
            String title,
            String content
    ) {
        this.user = user;
        this.title = title;
        this.content = content;

        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    // 게시글 수정
    public void update(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
    }

    // 좋아요 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    // 댓글 증가
    public void increaseCommentCount() {
        this.commentCount++;
    }

    // 댓글 감소
    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}