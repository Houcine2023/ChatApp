package com.SummerProject.Skote.Controllers;


import com.SummerProject.Skote.DTO.LoginRequest;
import com.SummerProject.Skote.DTO.RegisterRequest;
import com.SummerProject.Skote.Services.UserServiceImpl;
import com.SummerProject.Skote.models.User;
import com.SummerProject.Skote.shared.GlobalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST})
public class AuthController {
    private final UserServiceImpl userService;

    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<GlobalResponse<String>> register(@RequestBody RegisterRequest registerRequest){
        User user =userService.registerUser(registerRequest);
        return new ResponseEntity<>(new GlobalResponse<>(user.getId().toString()), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<String>> login(@RequestBody LoginRequest loginRequest){
        User user = userService.authenticate(loginRequest);
        return new ResponseEntity<>(new GlobalResponse<>(user.getId().toString()), HttpStatus.OK);
    }


}
