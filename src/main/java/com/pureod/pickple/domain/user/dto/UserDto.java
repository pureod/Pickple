package com.pureod.pickple.domain.user.dto;

import com.pureod.pickple.domain.user.enums.Role;
import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    Instant createdAt,
    String email,
    String name,
    String profileImageUrl,
    Role role,
    Boolean locked

) {

}
