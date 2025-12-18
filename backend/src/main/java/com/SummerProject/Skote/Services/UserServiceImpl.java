package com.SummerProject.Skote.Services;

import com.SummerProject.Skote.DTO.LoginRequest;
import com.SummerProject.Skote.DTO.RegisterRequest;
import com.SummerProject.Skote.DTO.Status;
import com.SummerProject.Skote.DTO.UserSearchResponse;
import com.SummerProject.Skote.Repositories.UserRepo;
import com.SummerProject.Skote.abstracts.UserService;
import com.SummerProject.Skote.models.User;
import com.SummerProject.Skote.shared.CustomeResponseException;
import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public UserServiceImpl(UserRepo userRepo,
                           PasswordEncoder passwordEncoder,
                           SimpMessagingTemplate simpMessagingTemplate) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepo.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepo.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(Instant.now());
        return userRepo.save(user);
    }

    @Override
    public User authenticate(LoginRequest loginRequest) {
        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> CustomeResponseException.resourceNotFound(
                        "User with email " + loginRequest.getEmail() + " not found"
                ));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw CustomeResponseException.invalidCredentials("Invalid password");
        }

        return user;
    }


    public List<UserSearchResponse> searchUsers(String query, UUID userId, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        List<User> users = userRepo.findByUsernameOrEmailContaining(query, userId);
        return users.stream()
                .map(user -> new UserSearchResponse(user.getId().toString(), user.getUsername(), user.getEmail(),user.getProfilePictureUrl()))
                .collect(Collectors.toList());
    }

    public boolean existsById(UUID userId) {
        return userRepo.existsById(userId); // Assumes UserRepo has this method
    }

    public User getUserById(UUID userId) {
        return userRepo.findById(userId).orElseThrow(() -> CustomeResponseException.resourceNotFound("User with id " + userId + " not found"));
    }

    public List<UserSearchResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(user -> new UserSearchResponse(
                        user.getId().toString(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getProfilePictureUrl()
                ))
                .collect(Collectors.toList());
    }


}