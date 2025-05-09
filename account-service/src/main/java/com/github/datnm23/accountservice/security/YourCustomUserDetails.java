package com.github.datnm23.accountservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.datnm23.accountservice.entity.User;

public class YourCustomUserDetails implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final boolean active;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    public YourCustomUserDetails(UUID userId, String username, String password, boolean active, boolean emailVerified, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.active = active;
        this.emailVerified = emailVerified;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }

    public static YourCustomUserDetails create(User user) {
        List<GrantedAuthority> authorities = Collections.emptyList();

        return new YourCustomUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                user.isEmailVerified(),
                authorities
        );
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return this.active && this.emailVerified;
    }
}
