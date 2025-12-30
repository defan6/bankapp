package com.bankapp.userservice.mapper;

import com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse;
import com.bankapp.userservice.domain.RefreshToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

//    @Mapping(target = "userId", expression = "java(user == null ? null: user.getId())")
    RefreshTokenResponse toResponse(RefreshToken refreshToken);
}
