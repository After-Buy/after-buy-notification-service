package com.After_Buy.NotificationService.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 스프링 시큐리티 유저 컨텍스트 모델
 * 검문 필터를 이겨낸 정상 사용자의 인증 사실을 인증 스레드 공간(보안 헤드 컨텍스트)에 체류시키기 위한 UserDetails 파생 인증 덩어리 객체입니다.
 *
 * @since : 2026.04.11
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    // 식별 인증된 유저의 매핑 DB PK 아이디
    private final Long userId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(this.userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
