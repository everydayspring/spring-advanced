package org.example.expert.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.config.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j(topic = "AdminAccess")
@Aspect
@Configuration
@RequiredArgsConstructor
public class AdminAccessAop {

    private final JwtUtil jwtUtil;

    @Pointcut("@annotation(org.example.expert.annotation.Admin)")
    private void adminAnnotation(){}


    @Before("adminAnnotation()")
    public void adminRequest() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String bearerJwt = request.getHeader("Authorization");
        String jwt = jwtUtil.substringToken(bearerJwt);
        Claims claims = jwtUtil.extractClaims(jwt);

        log.info("ID : {}",String.valueOf(Long.parseLong(claims.getSubject())));

        log.info("DATETIME : {} T{}", String.valueOf(LocalDate.now()), String.valueOf(LocalTime.now()));

        log.info("Request URL : {}",String.valueOf(request.getRequestURL()));
    }
}
