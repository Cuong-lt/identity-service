package com.ltcuong.identity_service.service;


import com.ltcuong.identity_service.dto.request.UserCreationRequest;
import com.ltcuong.identity_service.dto.request.UserUpdateRequest;
import com.ltcuong.identity_service.dto.response.UserResponse;
import com.ltcuong.identity_service.enums.Role;
import com.ltcuong.identity_service.exception.AppException;
import com.ltcuong.identity_service.exception.ErrorCode;
import com.ltcuong.identity_service.entity.User;
import com.ltcuong.identity_service.mapper.UserMapper;
import com.ltcuong.identity_service.repository.RoleRepository;
import com.ltcuong.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        // mã hóa password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // add role vào user
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

//        user.setRoles(roles);


        return userMapper.toUserResponse(userRepository.save(user));
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAuthority('APPROVE_POST')")
    // kiểm tra role trước lúc sử dụng hàm
    public List<UserResponse> getUsers() {
    log.info("In method log users");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    // kiểm tra sau khi chạy method
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(userRepository.findById(id).
                orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_EXISTED.getMessage())));
    }

    // lấy info user
    public UserResponse getMyinfo(){
        // lấy thông tin user từ SecurityContextHolder
        // sau khi authen thì thông tin user được lưu trong SecurityContextHolder
        var context = SecurityContextHolder.getContext();

        // lấy ra tên của user đang đăng nhập
        String name = context.getAuthentication().getName();

        // tìm user trong database qua repository
        // gán user tìm được vào entity
        User user = userRepository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public String deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.deleteById(userId);

        return "Delete successfully";
    }
}
