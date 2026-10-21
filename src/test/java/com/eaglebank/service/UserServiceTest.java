package com.eaglebank.service;

import com.eaglebank.domain.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.web.dto.AddressDto;
import com.eaglebank.web.dto.CreateUserRequest;
import com.eaglebank.web.dto.UserResponse;
import com.eaglebank.web.exception.DomainExceptions.ForbiddenOperationException;
import com.eaglebank.web.exception.DomainExceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test suite of the class: UserService")
public class UserServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-21T12:00:00Z");

    private UserService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new UserService(userRepository, passwordEncoder, fixedClock);
    }

    @ParameterizedTest
    @MethodSource("blankOrNullPasswords")
    @DisplayName("Should use default password when password is null or blank")
    void givenNullOrBlankPassword_whenCreateUser_thenShouldEncodeDefaultPassword(String password) {
        //given
        when(passwordEncoder.encode(eq(UserService.DEFAULT_PASSWORD))).thenReturn("hashed-default");
        CreateUserRequest request = new CreateUserRequest(
                "John Doe",
                dummyAddress(),
                "+447700900111",
                "john@eaglebank.com",
                password
        );

        //when
        UserResponse response = service.createUser(request);

        //then
        assertNotNull(response);
        assertTrue(response.id().startsWith("usr-"));
        assertEquals("John Doe", response.name());
        assertEquals(FIXED_NOW, response.createdTimestamp());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-default", captor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("Should encode custom password when provided")
    void givenCustomPassword_whenCreateUser_thenShouldEncodeCustomPassword() {
        //given
        when(passwordEncoder.encode(eq("MySecret123!"))).thenReturn("hashed-custom");
        CreateUserRequest request = new CreateUserRequest(
                "Atilla Barros",
                dummyAddress(),
                "+447700900333",
                "atilla@eaglebank.com",
                "MySecret123!"
        );

        //when
        UserResponse response = service.createUser(request);

        //then
        assertNotNull(response);
        assertEquals("atilla@eaglebank.com", response.email());
        verify(passwordEncoder).encode(eq("MySecret123!"));
    }

    @Test
    @DisplayName("Should fetch user details when requested by owner")
    void givenExistingUserIdAndOwner_whenFetchUserById_thenShouldReturnUser() {
        //given
        String userId = "usr-alice1";
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(dummyUser(userId)));

        //when
        UserResponse response = service.fetchUserById(userId, userId);

        //then
        assertNotNull(response);
        assertEquals(userId, response.id());
        verify(userRepository).findById(eq(userId));
    }

    @Test
    @DisplayName("Should NOT fetch user details when requested by another user")
    void givenDifferentUserId_whenFetchUserById_thenShouldThrowForbiddenException() {
        //given
        String userId = "usr-alice1";
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(dummyUser(userId)));

        //when
        Executable fetchUser = () -> service.fetchUserById(userId, "usr-bob2");

        //then
        assertThrows(ForbiddenOperationException.class, fetchUser);
    }

    @Test
    @DisplayName("Should NOT fetch user details when user does not exist")
    void givenNonExistingUserId_whenFetchUserById_thenShouldThrowNotFoundException() {
        //given
        String userId = "usr-missing";
        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        //when
        Executable fetchUser = () -> service.fetchUserById(userId, userId);

        //then
        assertThrows(ResourceNotFoundException.class, fetchUser);
    }

    private User dummyUser(String userId) {
        return new User(
                userId,
                "Atilla Barros",
                dummyAddress().toDomain(),
                "+447700900333",
                "atilla@eaglebank.com",
                "hash",
                FIXED_NOW,
                FIXED_NOW
        );
    }

    private AddressDto dummyAddress() {
        return new AddressDto("1 Churchill Place", "Canary Wharf", null, "London", "Greater London", "E14 5HP");
    }

    private static Stream<Arguments> blankOrNullPasswords() {
        return Stream.of(
                arguments((String) null),
                arguments(""),
                arguments("   ")
        );
    }
}
