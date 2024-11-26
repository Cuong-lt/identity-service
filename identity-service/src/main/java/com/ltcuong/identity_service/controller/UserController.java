package com.ltcuong.identity_service.controller;


import com.ltcuong.identity_service.dto.response.ApiResponse;
import com.ltcuong.identity_service.dto.request.UserCreationRequest;
import com.ltcuong.identity_service.dto.request.UserUpdateRequest;
import com.ltcuong.identity_service.dto.response.UserResponse;
import com.ltcuong.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){

        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(request))
                .build();
    }

    @GetMapping()
    ApiResponse<List<UserResponse>> getUsers(){
        // kiem tra thong tin dang nhap cua user
        // SecurityContextHolder giup luu thong tin cua user khi dang nhap
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        //log ra console thong tin user hien tai trong request
        log.info("Username: {}",authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId){
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUser(userId))
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyinfo())
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser( @PathVariable("userId") String userId,@RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUser(userId,request))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String>  deleteUser(@PathVariable("userId") String userId){
       return ApiResponse.<String>builder()
               .data("User has been deleted")
               .build();
    }
}
