package com.pureod.pickple.domain.user.mapper;

import com.pureod.pickple.domain.user.dto.UserDto;
import com.pureod.pickple.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

}
