package com.SummerProject.Skote.Controllers;

import com.SummerProject.Skote.DTO.UserSearchResponse;
import com.SummerProject.Skote.Services.UserServiceImpl;
import com.SummerProject.Skote.models.User;
import com.SummerProject.Skote.shared.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam UUID userId) {
        if (query == null || query.trim().isEmpty()) {
            return new ResponseEntity<>(new GlobalResponse<>(List.of()), HttpStatus.BAD_REQUEST);
        }
        List<UserSearchResponse> results = userService.searchUsers(query, userId, limit);
        return new ResponseEntity<>(new GlobalResponse<>(results), HttpStatus.OK);
    }
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable UUID userId){
        return userService.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<UserSearchResponse>>> getAllUsers(){
    return new ResponseEntity<>(new GlobalResponse<>(userService.getAllUsers()), HttpStatus.OK);
    }

}