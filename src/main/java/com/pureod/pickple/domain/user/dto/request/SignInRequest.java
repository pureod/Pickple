package com.pureod.pickple.domain.user.dto.request;

public record SignInRequest(
    String username,
    String password
) {

}
