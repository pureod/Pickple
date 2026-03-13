package com.pureod.pickple.domain.auth.dto;

import com.pureod.pickple.domain.user.dto.UserDto;

public record JwtDto(
    String accessToken,
    UserDto userDto
) {}