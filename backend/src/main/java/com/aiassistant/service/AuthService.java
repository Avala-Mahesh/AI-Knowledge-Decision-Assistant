package com.aiassistant.service;

import com.aiassistant.dto.AuthRequest;
import com.aiassistant.dto.AuthResponse;
import com.aiassistant.dto.RegisterRequest;
import com.aiassistant.entity.Organization;
import com.aiassistant.entity.User;
import com.aiassistant.exception.ResourceNotFoundException;
import com.aiassistant.repository.OrganizationRepository;
import com.aiassistant.repository.UserRepository;
import com.aiassistant.util.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists.");
        }

        String slug = request.getOrganizationName().trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            slug = "org-" + System.currentTimeMillis();
        }

        if (organizationRepository.existsBySlug(slug)) {
            slug = slug + "-" + (System.currentTimeMillis() % 10000);
        }

        Organization organization = new Organization(request.getOrganizationName().trim(), slug);
        Organization savedOrg = organizationRepository.save(organization);

        User user = new User(
                savedOrg,
                request.getEmail().trim().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim()
        );
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getId(), savedOrg.getId(), savedUser.getEmail(), savedUser.getRole());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedOrg.getId(),
                savedOrg.getName(),
                savedOrg.getAssistantToken()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        Organization org = user.getOrganization();
        String token = jwtUtil.generateToken(user.getId(), org.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                org.getId(),
                org.getName(),
                org.getAssistantToken()
        );
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }
}
