package com.bankapp.userservice.mapper;

import com.bankapp.common.client.userservice.model.RegisterRequest;
import com.bankapp.common.client.userservice.model.RegisterResponse;
import com.bankapp.userservice.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(RegisterRequest request);

    @Mapping(source = "id", target = "userId")
    RegisterResponse toRegisterResponse(User user);
}
