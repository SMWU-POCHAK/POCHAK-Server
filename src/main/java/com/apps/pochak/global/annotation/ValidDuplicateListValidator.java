package com.apps.pochak.global.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ValidDuplicateListValidator implements ConstraintValidator<ValidDuplicateList, List<String>> {
    @Override
    public boolean isValid(final List<String> stringList, final ConstraintValidatorContext constraintValidatorContext) {
        final long uniqueCount = stringList.stream().distinct().count();
        return uniqueCount == stringList.size();
    }
}
