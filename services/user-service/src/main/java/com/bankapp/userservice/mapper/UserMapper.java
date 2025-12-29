package com.bankapp.userservice.mapper;


import com.bankapp.common.client.userserviceuser.model.UserResponse;
import com.bankapp.userservice.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
