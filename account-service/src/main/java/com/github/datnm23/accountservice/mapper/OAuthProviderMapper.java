package com.github.datnm23.accountservice.mapper;

import com.github.datnm23.accountservice.dto.OAuthProviderDTO;
import com.github.datnm23.accountservice.entity.OAuthProvider;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

public interface OAuthProviderMapper {
    // Cố ý bỏ qua các mã thông báo truy cập và làm mới trong DTO
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    OAuthProviderDTO toDto(OAuthProvider OAuthProvider);

    //Khi ánh xạ ngược lại, giữ nguyên secret token hiện tại nếu DTO không cung cấp secret token
    @InheritInverseConfiguration
    @Mapping(target = "accessToken",
            expression = "java(dto.getAccessToken() != null ? dto.getAccessToken() : entity.getAccessToken())")
    @Mapping(target = "refreshToken",
            expression = "java(dto.getRefreshToken()!= null ? dto.getRefreshToken(): entity.getRefreshToken())")
    void updateEntity(@MappingTarget OAuthProvider entity, OAuthProviderDTO dto);
}
