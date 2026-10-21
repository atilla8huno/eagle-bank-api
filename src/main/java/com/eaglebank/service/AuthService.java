package com.eaglebank.service;


import com.eaglebank.domain.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.security.Permission;
import com.eaglebank.security.Permission.Role;
import com.eaglebank.web.dto.AuthDtos;
import com.eaglebank.web.dto.AuthDtos.AuthTokenRequest;
import com.eaglebank.web.dto.AuthDtos.AuthTokenResponse;
import com.eaglebank.web.exception.DomainExceptions;
import com.eaglebank.web.exception.DomainExceptions.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final String issuer;
    private final long expirationSeconds;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            Clock clock,
            @Value("${eaglebank.security.jwt.issuer}") String issuer,
            @Value("${eaglebank.security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse issueToken(AuthTokenRequest request) {
        String identifier = request.username().trim();
        User user = (identifier.startsWith("usr-")
                ? userRepository.findById(identifier)
                : userRepository.findFirstByEmailIgnoreCase(identifier))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid user credentials supplied"));

        String rawPassword = (request.password() == null || request.password().isBlank())
                ? UserService.DEFAULT_PASSWORD
                : request.password();

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid user credentials supplied");
        }

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", Role.CUSTOMER.name())
                .claim("scope", Role.CUSTOMER.toScopeClaim())
                .build();

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
        return new AuthTokenResponse(tokenValue, "Bearer", expirationSeconds);
    }
}
