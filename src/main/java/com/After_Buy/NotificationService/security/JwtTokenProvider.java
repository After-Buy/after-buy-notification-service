package com.After_Buy.NotificationService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 토큰 파싱 및 검증 프로바이더 (Notification Service 전용)
 * Auth Service와 동일한 JWT 비밀키를 공유하여 앱에서 넘어온 Access Token을 복호화하고 유저 ID를 추출합니다.
 * 이 서비스는 토큰을 발급하지 않으며, 오직 검증 및 파싱만 수행합니다.
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 정상 토큰 코어 데이터 복호화 디코드 처리
     * 검증 필터를 지난 외부 문자열 토큰의 서명 락을 풀어 주입되어있는 원래의 유저 ID 식별값을 강제로 추출해냅니다.
     *
     * @param token : 파싱을 시도할 암호 문자 원본
     * @return : 내재되어있던 유저의 데이터베이스 연동 PK 수동 반환
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 서명 및 형식 무결성 점검 보안 판독 기능
     * 위조, 변조되거나 기간이 지난 토큰을 잡아내어 불량 처리하기 위한 부울린 반환형 탐지기 역할을 수행합니다.
     *
     * @param token : 클레임 파싱 무결성 점검을 시도할 암호 문자
     * @return : 정상적인 수명의 조작 없는 깨끗한 토큰일 시 true, 불량품이거나 만료 시 false 리턴
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
    }
}
