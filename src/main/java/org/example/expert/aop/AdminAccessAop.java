package org.example.expert.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.config.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j(topic = "AdminAccess")
@Aspect
@Configuration
@RequiredArgsConstructor
public class AdminAccessAop {

    private final JwtUtil jwtUtil;

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))")
    private void forCommentAdmin() {}

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    private void forUserAdmin() {}

    @Before("forCommentAdmin() || forUserAdmin()")
    public void adminRequest(JoinPoint joinPoint) throws UnsupportedEncodingException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String bearerJwt = request.getHeader("Authorization");
        String jwt = jwtUtil.substringToken(bearerJwt);
        Claims claims = jwtUtil.extractClaims(jwt);

        log.info("ID : {}",String.valueOf(Long.parseLong(claims.getSubject())));

        log.info("DATETIME : {} T{}", String.valueOf(LocalDate.now()), String.valueOf(LocalTime.now()));

        log.info("Request URL : {}",String.valueOf(request.getRequestURL()));
    }
}
