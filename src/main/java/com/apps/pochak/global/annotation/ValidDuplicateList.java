package com.apps.pochak.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = {ElementType.PARAMETER, ElementType.FIELD})
@Retention(value = RUNTIME)
@Constraint(validatedBy = ValidDuplicateListValidator.class)
public @interface ValidDuplicateList {
    String message() default "Duplicate List";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
