package md.cernev.minimemo.mapper;

import md.cernev.minimemo.dto.SignUpDto;
import md.cernev.minimemo.dto.UserDto;
import md.cernev.minimemo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);
}
