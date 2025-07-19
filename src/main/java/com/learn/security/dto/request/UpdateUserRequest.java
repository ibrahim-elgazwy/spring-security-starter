package com.learn.security.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    public String name;
    public String email;
}
