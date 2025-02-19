package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserDto;
import ru.practicum.dto.user.UserDto;

import java.util.Collection;

@Service
public interface UserService {

    UserDto newUser(NewUserDto dto);

    Collection<UserDto> getAllUsers(int from, int size);

    void deleteUser(long userId);
}
