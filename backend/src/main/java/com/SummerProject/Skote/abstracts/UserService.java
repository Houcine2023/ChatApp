package com.SummerProject.Skote.abstracts;

import com.SummerProject.Skote.DTO.LoginRequest;
import com.SummerProject.Skote.DTO.RegisterRequest;
import com.SummerProject.Skote.models.User;

public interface UserService
{
    public User registerUser(RegisterRequest registerRequest);
    public User authenticate(LoginRequest loginRequest);
}
