package md.cernev.minimemo.service;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.dto.CredentialsDto;
import md.cernev.minimemo.dto.SignUpDto;
import md.cernev.minimemo.dto.UserDto;
import md.cernev.minimemo.entity.User;
import md.cernev.minimemo.mapper.UserMapper;
import md.cernev.minimemo.repository.UserRepository;
import md.cernev.minimemo.util.CustomHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserDto> findByLogin(String login) {
        Mono<Optional<User>> userMono = Mono.fromFuture(userRepository.findByLogin(login));
        return userMono.map(optionalUser -> optionalUser
            .map(userMapper::toUserDto)
            .orElseThrow(() -> new CustomHttpException("User not found", HttpStatus.NOT_FOUND)));
    }

    public Mono<UserDto> login(CredentialsDto credentialsDto) {
        Mono<Optional<User>> userMono = Mono.fromFuture(userRepository.findByLogin(credentialsDto.getLogin()));
        return userMono.map(optionalUser -> optionalUser
            .map(user -> {
                if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
                    return userMapper.toUserDto(user);
                } else {
                    throw new CustomHttpException("Invalid password", HttpStatus.BAD_REQUEST);
                }
            })
            .orElseThrow(() -> new CustomHttpException("User not found", HttpStatus.NOT_FOUND)));
    }

    public Mono<UserDto> register(SignUpDto signUpDto) {
        Mono<Optional<User>> userMono = Mono.fromFuture(userRepository.findByLogin(signUpDto.getLogin()));
        return userMono.flatMap(user -> {
            if (user.isPresent()) {
                return Mono.error(new CustomHttpException("User already exists", HttpStatus.BAD_REQUEST));
            }
            User userToSave = userMapper.signUpToUser(signUpDto);
            userToSave.setId(UUID.randomUUID().toString());
            userToSave.setPassword(passwordEncoder.encode(CharBuffer.wrap(signUpDto.getPassword())));
            return Mono.fromFuture(userRepository.save(userToSave))
                .map(userMapper::toUserDto);
        });
    }
}
