package com.pureod.pickple.domain.user.service;

import com.pureod.pickple.domain.user.dto.UserDto;
import com.pureod.pickple.domain.user.dto.request.UserCreateRequest;
import com.pureod.pickple.domain.user.dto.request.UserUpdateRequest;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserDto userCreated(UserCreateRequest userCreateRequest);

    UserDto updateProfile(UUID targetUserId, UUID requesterUserId, UserUpdateRequest request,
        MultipartFile image);
}
