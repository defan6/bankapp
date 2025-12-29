package com.bankapp.userservice.mapper;

import com.bankapp.userservice.domain.RefreshToken;
import com.bankapp.userservice.domain.token.dto.RefreshTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    @Mapping(target = "userId", expression = "java(user == null ? null: user.getId())")
    RefreshTokenResponse toResponse(RefreshToken refreshToken);
}
