package com.SummerProject.Skote.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomeResponseException extends RuntimeException {

    private final int code;
    private final String message;

    // Existing method
    public static CustomeResponseException resourceNotFound(String message) {
        return new CustomeResponseException(404, message);
    }

    // ✅ Add this method for login/password failures
    public static CustomeResponseException invalidCredentials(String message) {
        return new CustomeResponseException(401, message); // 401 = Unauthorized
    }

    // ✅ Optional: Bad request helper
    public static CustomeResponseException badRequest(String message) {
        return new CustomeResponseException(400, message);
    }
}
