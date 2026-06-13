package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private int commentId;

    @ManyToOne(fetch = FetchType.LAZY) // 댓글(N)은 하나의 사용자(1)가 작성 가능
    @JoinColumn(name = "user_id") // comments.user_id FK
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 댓글(N)은 하나의 게시글(1)에 속함
    @JoinColumn(name = "post_id") // comments.post_id FK
    private Post post;

    @Column(nullable = false)
    private String content;

    public Comment(
            User user,
            Post post,
            String content
    ) {
        this.user = user;
        this.post = post;
        this.content = content;
    }

    // 댓글 수정
    public void update(
            String content
    ) {
        this.content = content;
    }
}
