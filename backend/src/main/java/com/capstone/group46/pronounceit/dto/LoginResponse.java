package com.capstone.group46.pronounceit.dto;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String role;
    private Integer accumulatedPoints;

    public LoginResponse(String token, Long userId, String email, String role, Integer accumulatedPoints) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.accumulatedPoints = accumulatedPoints;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
    public String getRole(){
        return role;
    }
    
    public Integer getAccumulatedPoints() {
        return accumulatedPoints;
    }
}
