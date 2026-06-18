package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.user.AuthCheckResponseDto;
import com.example._ayelcommunitybe.dto.user.LoginRequestDto;
import com.example._ayelcommunitybe.dto.user.LoginResponseDto;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    // 로그인
    @PostMapping
    public ApiResponse<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequest,
            HttpServletRequest request
    ) {

        User user = userService.login(loginRequest);

        // 세션 생성
        HttpSession session = request.getSession();

        // 세션에 회원 ID 저장
        session.setAttribute(
                SessionConst.USER_ID,
                user.getUserId()
        );

        return ApiResponse.success(
                "로그인 성공",
                new LoginResponseDto(
                        user.getUserId()
                )
        );
    }

    // 로그아웃
    @DeleteMapping
    public ApiResponse<Void> logout(
            HttpServletRequest request
    ) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ApiResponse.success(
                "로그아웃 성공"
        );
    }

    // 로그인 상태 확인
    @GetMapping("/check")
    public ApiResponse<AuthCheckResponseDto> checkLogin(
            @RequestAttribute(SessionConst.USER_ID) int userId
    ) {

        User user = userService.getEntity(userId);
        String profileFileUrl = userService.getProfileFileUrl(user);

        return ApiResponse.success(
                "로그인 상태 확인 성공",
                new AuthCheckResponseDto(
                        user.getUserId(),
                        user.getEmail(),
                        user.getNickname(),
                        profileFileUrl
                )
        );
    }
}