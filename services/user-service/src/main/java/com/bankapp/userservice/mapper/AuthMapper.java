package com.bankapp.userservice.mapper;

import com.bankapp.common.client.userserviceauth.model.RegisterResponse;
import com.bankapp.userservice.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {


    @Mapping(source = "id", target = "userId")
    RegisterResponse toRegisterResponse(User user);

}
