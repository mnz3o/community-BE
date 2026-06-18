package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.user.*;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.UserFinder;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserFinder userFinder;

    // 로그인
    public User login(LoginRequestDto request) {

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() ->
                        new CustomException(ErrorCode.INVALID_LOGIN));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        return user;
    }

    // 회원 조회
    public UserResponseDto getUser(int userId) {

        User user = userFinder.findById(userId);

        // 활성화된 프로필 파일 조회
        String profileFileUrl = storedFileRepository
                .findByUserAndIsActiveTrue(user)
                .map(StoredFile::getFileUrl)
                .orElse(null);

        return new UserResponseDto(
                user.getEmail(),
                user.getNickname(),
                profileFileUrl
        );
    }

    // 회원 정보 수정
    @Transactional
    public void updateUser(
            int sessionUserId,
            int userId,
            UserUpdateRequestDto request) {

        validateUserOwner(sessionUserId, userId);

        User user = userFinder.findById(userId);

        User duplicateUser = userRepository
                .findByNickname(request.nickname())
                .orElse(null);

        // 이미 사용 중인 닉네임인지 확인
        if (duplicateUser != null
                && duplicateUser.getUserId() != userId) {

            throw new CustomException(
                    ErrorCode.DUPLICATE_NICKNAME
            );
        }

        user.updateNickname(request.nickname());
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(
            int sessionUserId,
            int userId,
            PasswordUpdateRequestDto request) {

        validateUserOwner(sessionUserId, userId);

        User user = userFinder.findById(userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword())) {

            throw new CustomException(
                    ErrorCode.PASSWORD_MISMATCH
            );
        }

        user.updatePassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(
            int sessionUserId,
            int userId) {

        validateUserOwner(sessionUserId, userId);

        User user = userFinder.findById(userId);

        user.delete();
    }

    // 회원가입
    @Transactional
    public int signup(SignupRequestDto request) {

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(
                    ErrorCode.DUPLICATE_EMAIL
            );
        }

        // 비밀번호 확인
        if (!request.password().equals(
                request.passwordConfirm())) {

            throw new CustomException(
                    ErrorCode.PASSWORD_MISMATCH
            );
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(
                request.nickname())) {

            throw new CustomException(
                    ErrorCode.DUPLICATE_NICKNAME
            );
        }

        User user = new User(
                request.email(),
                passwordEncoder.encode(
                        request.password()
                ),
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        return savedUser.getUserId();
    }

    private void validateUserOwner(
            int sessionUserId,
            int userId) {

        // 본인만 회원 정보 수정 및 탈퇴 가능
        if (sessionUserId != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    // 로그인한 사용자 엔티티 조회
    public User getEntity(int userId) {
        return userFinder.findById(userId);
    }

    public String getProfileFileUrl(User user) {

        return storedFileRepository
                .findByUserAndIsActiveTrue(user)
                .map(StoredFile::getFileUrl)
                .orElse(null);
    }
}