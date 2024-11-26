package com.ltcuong.identity_service.mapper;

import com.ltcuong.identity_service.dto.request.PermissionRequest;
import com.ltcuong.identity_service.dto.response.PermissionResponse;
import com.ltcuong.identity_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
