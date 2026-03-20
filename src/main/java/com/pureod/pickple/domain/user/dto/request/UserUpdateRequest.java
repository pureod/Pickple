package com.pureod.pickple.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank(message = "이름은 공백일 수 없습니다.")
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하로 입력해주세요.")
    String name
) {

}
