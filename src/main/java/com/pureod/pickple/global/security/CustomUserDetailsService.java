package com.pureod.pickple.global.security;

import com.pureod.pickple.domain.user.entity.User;
import com.pureod.pickple.domain.user.mapper.UserMapper;
import com.pureod.pickple.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        if (user.isLocked()) {
            throw new UsernameNotFoundException("사용자 계정이 잠겨 있습니다: " + email);
        }

        return new PickpleUserDetails(userMapper.toDto(user));
    }
}