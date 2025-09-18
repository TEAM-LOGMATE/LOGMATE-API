package com.logmate.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String currentPassword;
    private String newEmail;
    private String newPassword;
}
