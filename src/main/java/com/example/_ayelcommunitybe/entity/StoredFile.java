package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stored_files")
public class StoredFile extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private int fileId;

    @OneToOne // 사용자(1)는 하나의 프로필 파일(0..1)을 가질 수 있음
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 파일(N)은 하나의 게시글(1)에 속할 수 있음
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public StoredFile(
            User user,
            Post post,
            String fileUrl
    ) {
        this.user = user;
        this.post = post;
        this.fileUrl = fileUrl;

        this.isActive = true;
    }

    // 파일 비활성화
    public void deactivate() {
        this.isActive = false;
    }
}