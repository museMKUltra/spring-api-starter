package com.codewithmosh.store.users;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    User toEntity(RegisterUserRequest request);

    void updateCurrent(UpdateCurrentUserRequest request, @MappingTarget User user);

    void update(UpdateUserRequest request, @MappingTarget User user);
}
