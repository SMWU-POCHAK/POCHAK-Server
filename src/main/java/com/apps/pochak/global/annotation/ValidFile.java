package com.apps.pochak.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = {ElementType.PARAMETER, ElementType.FIELD})
@Retention(value = RUNTIME)
@Constraint(validatedBy = ValidFileValidator.class)
public @interface ValidFile {
    String message() default "Invalid File";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
