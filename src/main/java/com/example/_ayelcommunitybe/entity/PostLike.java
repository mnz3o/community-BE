package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "post_likes")
public class PostLike extends CreatedEntity {

    // user_id + post_id 복합키
    @EmbeddedId
    private PostLikeId postLikeId;

    @MapsId("userId") // PostLikeId.userId와 User PK 매핑
    @ManyToOne(fetch = FetchType.LAZY) // 좋아요(N)는 하나의 사용자(1)에 속함
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("postId") // PostLikeId.postId와 Post PK 매핑
    @ManyToOne(fetch = FetchType.LAZY) // 좋아요(N)는 하나의 게시글(1)에 속함
    @JoinColumn(name = "post_id")
    private Post post;

    public PostLike(
            User user,
            Post post
    ) {
        this.postLikeId = new PostLikeId(
                user.getUserId(),
                post.getPostId()
        );

        this.user = user;
        this.post = post;
    }
}