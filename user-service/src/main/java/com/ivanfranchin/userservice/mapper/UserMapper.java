package com.ivanfranchin.userservice.mapper;

import com.ivanfranchin.userservice.dto.CreateUserRequest;
import com.ivanfranchin.userservice.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.dto.UserResponse;
import com.ivanfranchin.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    User toUser(CreateUserRequest createUserRequest);

    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    void updateUserFromRequest(UpdateUserRequest updateUserRequest, @MappingTarget User user);
}
