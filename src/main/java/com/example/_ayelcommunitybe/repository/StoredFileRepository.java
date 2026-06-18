package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoredFileRepository
        extends JpaRepository<StoredFile, Integer> {

    // 활성화된 프로필 파일 조회
    Optional<StoredFile> findByUserAndIsActiveTrue(
            User user
    );

    // 활성화된 게시글 파일 조회
    Optional<StoredFile> findByFileIdAndIsActiveTrue(
            int fileId
    );

    // 사용자 프로필 파일 일괄 조회
    List<StoredFile> findByUserInAndIsActiveTrue(
            List<User> users
    );
}