package com.example.demo.dto;

import lombok.Data;

public class AuthRequest {

    @Data
    public static class SignupRequest {
        private String name;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
}
