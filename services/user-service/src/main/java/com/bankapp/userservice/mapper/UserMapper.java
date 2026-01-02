package com.bankapp.userservice.mapper;


import com.bankapp.common.client.userserviceuser.model.UserResponse;
import com.bankapp.userservice.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "userId")
    UserResponse toUserResponse(User user);
}
