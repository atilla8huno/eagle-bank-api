package com.eaglebank.service;

import com.eaglebank.domain.Address;
import com.eaglebank.domain.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.web.dto.AuthDtos.AuthTokenRequest;
import com.eaglebank.web.dto.AuthDtos.AuthTokenResponse;
import com.eaglebank.web.exception.DomainExceptions.AuthenticationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test suite of the class: AuthService")
class AuthServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-21T12:00:00Z");

    private AuthService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setup() {
        service = new AuthService(
                userRepository,
                passwordEncoder,
                jwtEncoder,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC),
                "https://auth.eaglebank.com",
                3600L
        );
    }

    @Test
    @DisplayName("Should issue JWT token when authenticating by userId or email")
    void givenValidCredentials_whenIssueToken_thenShouldReturnSignedJwtToken() {
        //given
        User user = dummyUser();
        when(userRepository.findById(eq("usr-abc123"))).thenReturn(Optional.of(user));
        when(userRepository.findFirstByEmailIgnoreCase(eq("atilla@eaglebank.com"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(UserService.DEFAULT_PASSWORD), eq("encoded-hash"))).thenReturn(true);
        when(passwordEncoder.matches(eq("CustomPass"), eq("encoded-hash"))).thenReturn(true);

        Jwt mockJwt = new Jwt(
                "signed-jwt-token",
                FIXED_NOW,
                FIXED_NOW.plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "usr-abc123")
        );
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        //when
        AuthTokenResponse resByUserId = service.issueToken(new AuthTokenRequest("usr-abc123", null));
        AuthTokenResponse resByEmail = service.issueToken(new AuthTokenRequest("atilla@eaglebank.com", "CustomPass"));
        AuthTokenResponse resWithBlankPass = service.issueToken(new AuthTokenRequest("usr-abc123", "   "));

        //then
        assertNotNull(resByUserId);
        assertEquals("signed-jwt-token", resByUserId.accessToken());
        assertEquals("Bearer", resByUserId.tokenType());
        assertEquals(3600L, resByUserId.expiresIn());
        assertEquals("signed-jwt-token", resByEmail.accessToken());
        assertEquals("signed-jwt-token", resWithBlankPass.accessToken());
    }

    @Test
    @DisplayName("Should NOT issue token when user does not exist or password does not match")
    void givenInvalidCredentials_whenIssueToken_thenShouldThrowAuthenticationFailedException() {
        //given
        User user = dummyUser();
        when(userRepository.findById(eq("usr-unknown"))).thenReturn(Optional.empty());
        when(userRepository.findById(eq("usr-abc123"))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("WrongPass"), eq("encoded-hash"))).thenReturn(false);

        //when
        Executable unknownUser = () -> service.issueToken(new AuthTokenRequest("usr-unknown", "pass"));
        Executable wrongPassword = () -> service.issueToken(new AuthTokenRequest("usr-abc123", "WrongPass"));

        //then
        assertThrows(AuthenticationFailedException.class, unknownUser);
        assertThrows(AuthenticationFailedException.class, wrongPassword);
    }

    private User dummyUser() {
        return new User(
                "usr-abc123",
                "Atilla Barros",
                new Address("1 St", null, null, "London", "Greater London", "E14 5HP"),
                "+447700900111",
                "atilla@eaglebank.com",
                "encoded-hash",
                FIXED_NOW,
                FIXED_NOW
        );
    }
}
