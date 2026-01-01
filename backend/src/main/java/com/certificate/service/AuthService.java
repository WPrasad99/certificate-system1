package com.certificate.service;

import com.certificate.dto.AuthResponse;
import com.certificate.dto.LoginRequest;
import com.certificate.dto.RegisterRequest;
import com.certificate.entity.Organizer;
import com.certificate.repository.OrganizerRepository;
import com.certificate.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (organizerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Organizer organizer = new Organizer();
        organizer.setFullName(request.getFullName());
        organizer.setEmail(request.getEmail());
        organizer.setPassword(passwordEncoder.encode(request.getPassword()));
        organizer.setInstituteName(request.getInstituteName());

        organizerRepository.save(organizer);

        String token = jwtUtil.generateToken(organizer.getEmail());

        return new AuthResponse(organizer.getId(), token, organizer.getEmail(), organizer.getFullName(),
                "Registration successful");
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Organizer organizer = organizerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(organizer.getEmail());

        return new AuthResponse(organizer.getId(), token, organizer.getEmail(), organizer.getFullName(),
                "Login successful");
    }

    public Organizer getOrganizerByEmail(String email) {
        return organizerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
    }
}
