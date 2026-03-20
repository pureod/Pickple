package com.pureod.pickple.domain.user.service;

import com.pureod.pickple.domain.user.dto.UserDto;
import com.pureod.pickple.domain.user.dto.request.UserCreateRequest;
import com.pureod.pickple.domain.user.dto.request.UserUpdateRequest;
import com.pureod.pickple.domain.user.entity.User;
import com.pureod.pickple.domain.user.enums.Role;
import com.pureod.pickple.domain.user.mapper.UserMapper;
import com.pureod.pickple.domain.user.repository.UserRepository;
import com.pureod.pickple.domain.user.storage.ProfileImageStorage;
import com.pureod.pickple.global.exception.ResourceNotFoundException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final long MAX_PROFILE_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageStorage profileImageStorage;

    @Transactional
    @Override
    public UserDto userCreated(UserCreateRequest userCreateRequest) {
        log.info("[Service] 사용자 생성 시작 - {}", userCreateRequest.name());

        String email = userCreateRequest.email();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userCreateRequest.password());

        User user = User.builder()
            .email(email)
            .name(userCreateRequest.name())
            .role(Role.USER)
            .password(encodedPassword)
            .locked(false)
            .build();

        User savedUser = userRepository.save(user);

        log.info("[Service] 사용자 생성 완료 - {}", savedUser.getName());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    @Override
    public UserDto updateProfile(UUID targetUserId, UUID requesterUserId, UserUpdateRequest request,
        MultipartFile image) {
        log.info("[Service] 프로필 변경 시작 - targetUserId={}, requesterUserId={}", targetUserId,
            requesterUserId);

        if (!targetUserId.equals(requesterUserId)) {
            throw new AccessDeniedException("본인 프로필만 수정할 수 있습니다.");
        }

        User user = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        String profileImageUrl = null;
        if (image != null && !image.isEmpty()) {
            validateProfileImage(image);
            profileImageUrl = profileImageStorage.upload(user.getId(), image);
        }

        user.updateProfile(request.name(), profileImageUrl);

        log.info("[Service] 프로필 변경 완료 - userId={}", user.getId());
        return userMapper.toDto(user);
    }

    private void validateProfileImage(MultipartFile image) {
        String contentType = image.getContentType();

        if (contentType == null || !ALLOWED_PROFILE_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("프로필 이미지는 JPG, PNG, WEBP 형식만 업로드할 수 있습니다.");
        }

        if (image.getSize() > MAX_PROFILE_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("프로필 이미지는 5MB 이하만 업로드할 수 있습니다.");
        }
    }
}
