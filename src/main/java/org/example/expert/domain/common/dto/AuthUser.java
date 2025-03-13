package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(Long id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
        this.nickname = nickname;
    }

    public UserRole getUserRole() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(UserRole::of)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("유저 역학을 찾을 수 없습니다"));
    }

}
