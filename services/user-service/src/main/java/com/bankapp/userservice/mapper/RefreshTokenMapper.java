package com.bankapp.userservice.mapper;

import com.bankapp.common.client.userserviceauth.model.RefreshTokenResponse;
import com.bankapp.userservice.domain.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    @Mapping(source = "token", target = "refreshToken")
    RefreshTokenResponse toResponse(RefreshToken refreshToken);
}
