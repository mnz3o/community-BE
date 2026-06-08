package com.example._ayelcommunitybe.security;

import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if (uri.equals("/auth") && method.equals("POST")) {
            return true;
        }

        if (uri.equals("/users") && method.equals("POST")) {
            return true;
        }

        if (uri.startsWith("/posts") && method.equals("GET")) {
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

        HttpSession session =
                request.getSession(false);

        if (
                session == null ||
                        session.getAttribute("user_id") == null
        ) {
            throw new CustomException(
                    ErrorCode.LOGIN_REQUIRED
            );
        }

        int userId =
                (int) session.getAttribute(
                        "user_id"
                );

        request.setAttribute(
                "user_id",
                userId
        );

        filterChain.doFilter(
                request,
                response
        );
    }
}