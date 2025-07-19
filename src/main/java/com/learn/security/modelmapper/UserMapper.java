package com.learn.security.modelmapper;

import com.learn.security.domain.User;
import com.learn.security.dto.UserDto;
import com.learn.security.dto.request.RegisterUserRequest;
import com.learn.security.dto.request.UpdateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}
