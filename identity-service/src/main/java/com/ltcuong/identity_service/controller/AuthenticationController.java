package com.ltcuong.identity_service.controller;


import com.ltcuong.identity_service.dto.request.LogoutRequest;
import com.ltcuong.identity_service.dto.request.RefreshRequest;
import com.ltcuong.identity_service.dto.response.ApiResponse;
import com.ltcuong.identity_service.dto.request.AuthenticationRequest;
import com.ltcuong.identity_service.dto.request.IntrospectRequest;
import com.ltcuong.identity_service.dto.response.AuthenticationResponse;
import com.ltcuong.identity_service.dto.response.IntrospectResponse;
import com.ltcuong.identity_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/token")
    // trả về api response
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){

        var result = authenticationService.authenticate(request);

        // map trường data của api response với trường authenticated của AuthenticationResponse với biến result.
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();

    }

    // xác minh token
    @PostMapping("/introspect")
        // trả về api response
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {

        var result = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .data(result)
                .build();
    }
    @PostMapping("/refresh")
        // trả về api response
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {

        var result = authenticationService.refreshToken(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();

    }

    @PostMapping("/logout")
        // trả về api response
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {

        authenticationService.logout(request);

        return ApiResponse.<Void>builder()
                .build();
    }

}
