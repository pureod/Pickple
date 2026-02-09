package com.pureod.pickple.domain.user.dto;

public record SignInRequest(
    String username,
    String password
) {

}
