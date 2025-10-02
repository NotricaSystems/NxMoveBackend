package com.next.move.security;

import com.next.move.models.UserProfile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final UserProfile user;

    public CustomUserDetails(UserProfile user) {
        this.user = user;
    }

    public UserProfile getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assuming you only have a single role field like "ROLE_USER"
        //return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
        return Collections.singletonList(new SimpleGrantedAuthority("CONSUMER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // or username, depending on your entity
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

