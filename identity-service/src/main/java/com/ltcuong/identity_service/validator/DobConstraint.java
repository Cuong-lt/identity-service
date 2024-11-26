package com.ltcuong.identity_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {DobValidator.class}
)
public @interface  DobConstraint {
    // 3 properties mặc định
    String message() default "Date of birth validation";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // properties custom
    int min();
}
