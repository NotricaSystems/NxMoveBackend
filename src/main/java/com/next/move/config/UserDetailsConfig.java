package com.next.move.config;

import com.next.move.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.next.move.security.CustomUserDetails;

@Configuration
public class UserDetailsConfig {

    private final UserRepository userRepository;

    public UserDetailsConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .map(CustomUserDetails::new) // wrap your User entity
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}

