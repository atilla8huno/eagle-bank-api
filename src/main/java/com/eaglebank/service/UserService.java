package com.eaglebank.service;

import com.eaglebank.domain.IdGenerator;
import com.eaglebank.domain.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.service.specification.UserSpecifications;
import com.eaglebank.web.dto.CreateUserRequest;
import com.eaglebank.web.dto.UserResponse;
import com.eaglebank.web.exception.DomainExceptions.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class UserService {
    public static final String DEFAULT_PASSWORD = "default-eaglebank-password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Instant now = Instant.now(clock);
        String rawPassword = resolvePassword(request.password());

        User user = new User(
                IdGenerator.nextUserId(),
                request.name(),
                request.address().toDomain(),
                request.phoneNumber(),
                request.email(),
                passwordEncoder.encode(rawPassword),
                now,
                now
        );

        userRepository.save(user);

        return UserResponse.fromDomain(user);
    }

    @Transactional(readOnly = true)
    public UserResponse fetchUserById(String userId, String authenticatedUserId) {
        User user = findUser(userId);

        UserSpecifications.assertOwnedBy(
                user,
                authenticatedUserId,
                "The user is not allowed to access the user details"
        );

        return UserResponse.fromDomain(user);
    }

    private User findUser(String userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private String resolvePassword(String password) {
        return (password == null || password.isBlank())
                ? DEFAULT_PASSWORD
                : password;
    }
}
