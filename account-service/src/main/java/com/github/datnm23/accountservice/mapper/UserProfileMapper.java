package com.github.datnm23.accountservice.mapper;

import com.github.datnm23.accountservice.dto.UserProfileDto;
import com.github.datnm23.accountservice.dto.UserProfileUpdateDto;
import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

    // Convert entity sang DTO
    UserProfileDto toDto(UserProfile userProfile);

    // Convert entity list sang DTO list
    List<UserProfileDto> toDtoList(List<UserProfile> userProfiles);

    // Tạo profile mới cho user
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "emailNotifications", constant = "true")
    @Mapping(target = "pushNotifications", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UserProfile createProfileFromUser(User user);

    // Update entity từ update DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProfileFromDto(UserProfileUpdateDto updateDto, @MappingTarget UserProfile userProfile);
}