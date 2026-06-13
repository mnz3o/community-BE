package com.example._ayelcommunitybe.security;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 인증이 필요 없는 요청은 필터 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 인증 없이 접근 가능한 API
        if (uri.endsWith("/auth") && method.equals("POST")) {
            return true;
        }

        if (uri.endsWith("/users") && method.equals("POST")) {
            return true;
        }

        // 공개 조회
        if (uri.startsWith("/posts") && method.equals("GET")) {
            return true;
        }

        // 업로드 파일 조회
        if (uri.startsWith("/uploads/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 기존 세션 조회
        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(SessionConst.USER_ID) == null) {

            response.setHeader(
                    "Access-Control-Allow-Origin",
                    "http://localhost:3000"
            );

            response.setHeader(
                    "Access-Control-Allow-Credentials",
                    "true"
            );

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            ApiResponse<Void> apiResponse =
                    ApiResponse.fail(
                            "로그인이 필요합니다."
                    );

            String jsonResponseBody =
                    objectMapper.writeValueAsString(
                            apiResponse
                    );

            response.getWriter().write(
                    jsonResponseBody
            );

            return;
        }

        int userId = (int) session.getAttribute(
                SessionConst.USER_ID
        );

        // 컨트롤러에서 사용할 사용자 ID 저장
        request.setAttribute(
                SessionConst.USER_ID,
                userId
        );

        filterChain.doFilter(
                request,
                response
        );
    }
}