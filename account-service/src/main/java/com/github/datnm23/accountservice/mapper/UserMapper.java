package com.github.datnm23.accountservice.mapper;

import com.github.datnm23.accountservice.dto.OAuthProviderDTO;
import com.github.datnm23.accountservice.dto.UserDTO;
import com.github.datnm23.accountservice.dto.UserUpdateDTO;
import com.github.datnm23.accountservice.dto.UserCreateDTO;
import com.github.datnm23.accountservice.dto.UserDetailDTO;
import com.github.datnm23.accountservice.entity.OAuthProvider;
import com.github.datnm23.accountservice.entity.User;
import com.github.datnm23.accountservice.entity.UserProfile;

import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDTO toUserDTO(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", constant = "PENDING_VERIFICATION")
    @Mapping(target = "active", constant = "false")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "oauthProviders", ignore = true)
    User toUser(UserCreateDTO userCreateDTO);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "oauthProviders", ignore = true)
    void updateUserFromDto(UserUpdateDTO userUpdateDTO, @MappingTarget User user);

    // Convert entity sang DTO
    @Mapping(target = "fullName",
            expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(target = "profile", source = "profile")
    @Mapping(target = "oauthProviders", qualifiedByName = "toDtoList")
    UserDTO toDto(User user);

    // Convert DTO sang entity
    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "oauthProviders", ignore = true)
    // manage separately
    User toEntity(UserDTO dto);


    @Named("toDtoList")
    default List<OAuthProviderDTO> mapProviders(List<com.github.datnm23.accountservice.entity.OAuthProvider> providers) {
        return providers == null
                ? java.util.Collections.emptyList()
                : providers.stream().map(this::providerToDto).toList();
    }

    OAuthProviderDTO providerToDto(OAuthProvider provider);


    // Keep only this implementation
    default List<UserDTO> toDtoList(List<User> users) {
        return users == null ? java.util.Collections.emptyList() 
                            : users.stream().map(this::toDto).toList();
    }

    // Convert registration DTO sang entity
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    User toEntity(UserCreateDTO userCreateDTO);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "active", source = "user.active")
    UserDetailDTO toUserDetailDTO(User user, UserProfile userProfile);
}