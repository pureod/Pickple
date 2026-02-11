package com.pureod.pickple.domain.user.service;

import com.pureod.pickple.domain.user.dto.request.UserCreateRequest;
import com.pureod.pickple.domain.user.dto.UserDto;
import com.pureod.pickple.domain.user.entity.User;
import com.pureod.pickple.domain.user.enums.Role;
import com.pureod.pickple.domain.user.mapper.UserMapper;
import com.pureod.pickple.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
}
