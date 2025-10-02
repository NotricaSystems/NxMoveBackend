package com.next.move.security;

import com.next.move.models.UserProfile;
import com.next.move.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println("Loading User...");
        // Delegate to the default implementation to fetch user info from provider
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Extract attributes from provider (Google/Facebook/etc.)
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google", "facebook"
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Find or create local User entity
        UserProfile user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserProfile newUser = new UserProfile();
                    newUser.setEmail(email);
                    return userRepository.save(newUser);
                });

        // Update user info if needed
        //user.setUsername(name);
        //userRepository.save(user);

        // Return a Spring Security OAuth2User with authorities
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }
}
