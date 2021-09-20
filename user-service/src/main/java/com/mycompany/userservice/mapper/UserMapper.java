package com.mycompany.userservice.mapper;

import com.mycompany.userservice.dto.CreateUserRequest;
import com.mycompany.userservice.dto.UpdateUserRequest;
import com.mycompany.userservice.dto.UserResponse;
import com.mycompany.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    User toUser(CreateUserRequest createUserRequest);

    UserResponse toUserResponse(User user);

    void updateUserFromRequest(UpdateUserRequest updateUserRequest, @MappingTarget User user);
}
