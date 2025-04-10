package com.github.datnm23.accountservice.mapper;


import com.github.datnm23.accountservice.dto.UserDto;
import com.github.datnm23.accountservice.dto.UserRegistrationDto;
import com.github.datnm23.accountservice.dto.UserUpdateDto;
import com.github.datnm23.accountservice.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Convert entity sang DTO
    UserDto toDto(User user);

    // Convert entity list sang DTO list
    List<UserDto> toDtoList(List<User> users);

    // Convert registration DTO sang entity
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isEmailVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    User toEntity(UserRegistrationDto registrationDto);

    // Update entity từ update DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDto updateDto, @MappingTarget User user);
}