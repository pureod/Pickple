package com.pureod.pickple.domain.user.service;

import com.pureod.pickple.domain.user.dto.request.UserCreateRequest;
import com.pureod.pickple.domain.user.dto.UserDto;

public interface UserService {

    UserDto userCreated(UserCreateRequest userCreateRequest);
}
