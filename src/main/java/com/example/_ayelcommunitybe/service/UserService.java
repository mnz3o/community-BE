package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.user.*;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 로그인
    public User login(
            LoginRequestDto request
    ) {

        User user = userRepository.findByEmail(
                        request.email()
                )
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.INVALID_LOGIN
                        )
                );

        if (
                !user.getPassword().equals(
                        request.password()
                )
        ) {
            throw new CustomException(
                    ErrorCode.INVALID_LOGIN
            );
        }

        return user;
    }

    // 회원 조회
    public UserResponseDto getUser(
            int userId
    ) {

        User user = findUser(userId);

        return new UserResponseDto(
                user.getEmail(),
                user.getNickname()
        );
    }

    // 회원 정보 수정
    @Transactional
    public void updateUser(
            int sessionUserId,
            int userId,
            UserUpdateRequestDto request
    ) {

        // 본인만 수정 가능
        validateUserOwner(
                sessionUserId,
                userId
        );

        User user = findUser(userId);

        User duplicateUser = userRepository.findByNickname(
                request.nickname()
        ).orElse(null);

        // 이미 사용 중인 닉네임인지 확인
        if (
                duplicateUser != null &&
                        duplicateUser.getUserId() != userId
        ) {
            throw new CustomException(
                    ErrorCode.DUPLICATE_NICKNAME
            );
        }

        user.updateNickname(
                request.nickname()
        );
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(
            int sessionUserId,
            int userId,
            PasswordUpdateRequestDto request
    ) {

        // 본인만 수정 가능
        validateUserOwner(
                sessionUserId,
                userId
        );

        User user = findUser(userId);

        // 현재 비밀번호 확인
        if (
                !user.getPassword().equals(
                        request.currentPassword()
                )
        ) {
            throw new CustomException(
                    ErrorCode.PASSWORD_MISMATCH
            );
        }

        user.updatePassword(
                request.newPassword()
        );
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(
            int sessionUserId,
            int userId
    ) {

        // 본인만 탈퇴 가능
        validateUserOwner(
                sessionUserId,
                userId
        );

        User user = findUser(userId);

        user.delete();
    }

    // 회원가입
    @Transactional
    public int signup(
            SignupRequestDto request
    ) {

        // 이메일 중복 확인
        if (
                userRepository.existsByEmail(
                        request.email()
                )
        ) {
            throw new CustomException(
                    ErrorCode.DUPLICATE_EMAIL
            );
        }

        // 비밀번호 확인
        if (
                !request.password().equals(
                        request.passwordConfirm()
                )
        ) {
            throw new CustomException(
                    ErrorCode.PASSWORD_MISMATCH
            );
        }

        // 닉네임 중복 확인
        if (
                userRepository.existsByNickname(
                        request.nickname()
                )
        ) {
            throw new CustomException(
                    ErrorCode.DUPLICATE_NICKNAME
            );
        }

        User user = new User(
                request.email(),
                request.password(),
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        return savedUser.getUserId();
    }

    private void validateUserOwner(
            int sessionUserId,
            int userId
    ) {

        if (sessionUserId != userId) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private User findUser(
            int userId
    ) {
        return userRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    // 로그인한 사용자 엔티티 조회
    public User getEntity(
            int userId
    ) {

        return findUser(userId);
    }
}