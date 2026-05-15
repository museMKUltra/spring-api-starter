package com.codewithmosh.store.users;

import com.codewithmosh.store.attendance.PermissionDeniedException;
import com.codewithmosh.store.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public Iterable<UserDto> getAllUsers(String sortBy) {
        if (!authService.requirePermission(Permission.MANAGE_USERS)) {
            throw new PermissionDeniedException("You don't have access to all users");
        }

        if (!Set.of("name", "email").contains(sortBy)) {
            sortBy = "name";
        }

        return userRepository
                .findAll(Sort.by(sortBy))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUser(Long id) {
        if (!authService.requireSelfOrPermission(id, Permission.MANAGE_USERS)) {
            throw new PermissionDeniedException("You don't have access to this user");
        }

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.toDto(user);
    }

    public UserDto registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException();
        }

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public User updateCurrentUser(UpdateCurrentUserRequest request) {
        var user = authService.getCurrentUser();
        if (user == null) {
            new UserNotFoundException();
        }
        userMapper.updateCurrent(request, user);
        userRepository.save(user);

        return user;
    }

    public UserDto updateUser(Long id, UpdateUserRequest request) {
        if (!authService.requireSelfOrPermission(id, Permission.MANAGE_USERS)) {
            throw new PermissionDeniedException("You don't have access to this user");
        }

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userMapper.update(request, user);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public void deleteUser(Long id) {
        if (!authService.requireSelfOrPermission(id, Permission.MANAGE_USERS)) {
            throw new PermissionDeniedException("You don't have access to this user");
        }

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    public void changePassword(Long id, ChangePasswordRequest request) {
        if (!authService.requireSelfOrPermission(id, Permission.MANAGE_USERS)) {
            throw new PermissionDeniedException("You don't have access to this user");
        }

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new AccessDeniedException("Password does not match");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }
}
