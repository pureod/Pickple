package com.pureod.pickple.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하로 입력해주세요.")
    String name
) {

}
