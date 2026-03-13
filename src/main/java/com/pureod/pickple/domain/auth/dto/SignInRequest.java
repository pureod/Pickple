package com.pureod.pickple.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
    @NotBlank @Email String username,
    @NotBlank String password
) {}