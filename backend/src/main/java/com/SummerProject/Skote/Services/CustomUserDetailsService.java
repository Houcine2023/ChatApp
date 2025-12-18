package com.SummerProject.Skote.Services;

import com.SummerProject.Skote.Services.UserServiceImpl;
import com.SummerProject.Skote.models.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserServiceImpl userService; // Inject UserService to validate userId

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UUID userId = UUID.fromString(username); // Assuming username is the userId string
            // Validate userId exists via UserService
            if (userService.existsById(userId)) {
                return new UserPrincipal(userId);
            }
            throw new UsernameNotFoundException("User with ID " + username + " not found");
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid userId format: " + username);
        }
    }
}