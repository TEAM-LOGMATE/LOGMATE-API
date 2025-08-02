package com.logmate.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String password;
}
