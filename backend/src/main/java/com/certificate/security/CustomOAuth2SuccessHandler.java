package com.certificate.security;

import com.certificate.entity.Organizer;
import com.certificate.repository.OrganizerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OrganizerRepository organizerRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Optional<Organizer> existingOrganizer = organizerRepository.findByEmail(email);
        Organizer organizer;

        if (existingOrganizer.isPresent()) {
            organizer = existingOrganizer.get();
            // Update details if needed
        } else {
            organizer = new Organizer();
            organizer.setEmail(email);
            organizer.setFullName(name);
            organizer.setPassword(""); // No password for OAuth users
            organizer.setInstituteName("Google Account"); // Default to generic or nullable
            organizerRepository.save(organizer);
        }

        String token = jwtUtil.generateToken(email);

        // Redirect to frontend with token and user info
        String redirectUrl = String.format("http://localhost:5173/oauth/callback?token=%s&fullName=%s&email=%s",
                token, java.net.URLEncoder.encode(name, "UTF-8"), java.net.URLEncoder.encode(email, "UTF-8"));
        response.sendRedirect(redirectUrl);
    }
}
