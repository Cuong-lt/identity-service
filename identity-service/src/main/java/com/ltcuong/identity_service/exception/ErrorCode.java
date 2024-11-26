package com.ltcuong.identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001,"User existed", HttpStatus.BAD_REQUEST),
    INVALID_KEY(1004, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1002,"Username at least {min} charactes", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003,"Password at least {min} charactes", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005,"User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006,"Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007,"You do not have permission",HttpStatus.FORBIDDEN),
    INVALID_DOB(1008,"You must be at least {min} years old",HttpStatus.BAD_REQUEST),
    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }




}
