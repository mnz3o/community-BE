package com.example._ayelcommunitybe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private int fileId;

    @OneToOne // 프로필 파일 (user 1 : 0..1 file)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne // 게시글 파일 (post 1 : 0..N file)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public StoredFile(
            User user,
            Post post,
            String fileUrl
    ) {
        this.user = user;
        this.post = post;
        this.fileUrl = fileUrl;

        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // 파일 비활성화
    public void deactivate() {
        this.isActive = false;
    }
}