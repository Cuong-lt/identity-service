package com.ltcuong.identity_service.controller;

import com.ltcuong.identity_service.dto.request.PermissionRequest;
import com.ltcuong.identity_service.dto.response.ApiResponse;
import com.ltcuong.identity_service.dto.response.PermissionResponse;
import com.ltcuong.identity_service.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@RequestMapping("/permissions")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> create (@RequestBody PermissionRequest request){
        return ApiResponse.<PermissionResponse>builder()
                .data(permissionService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAll(){
        return ApiResponse.<List<PermissionResponse>>builder()
                .data(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permissionName}")
    ApiResponse<Void> delete(@PathVariable("permissionName") String permissionName){
        permissionService.delete(permissionName);
        return ApiResponse.<Void>builder()
                .build();
    }
}
