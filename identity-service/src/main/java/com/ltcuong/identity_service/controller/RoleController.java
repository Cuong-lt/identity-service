package com.ltcuong.identity_service.controller;

import com.ltcuong.identity_service.dto.request.RoleRequest;
import com.ltcuong.identity_service.dto.response.ApiResponse;
import com.ltcuong.identity_service.dto.response.RoleResponse;
import com.ltcuong.identity_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/roles")
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .data(roleService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getRoles(){
        return ApiResponse.<List<RoleResponse>>builder()
                .data(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{roleName}")
    public  ApiResponse<Void> deleteRole(@PathVariable("roleName") String roleName){
        roleService.delete(roleName);
        return ApiResponse.<Void>builder()
                .build();
    }
}
