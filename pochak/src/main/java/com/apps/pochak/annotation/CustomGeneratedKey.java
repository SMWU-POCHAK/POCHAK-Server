package com.apps.pochak.annotation;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerated;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

@DynamoDBAutoGenerated(generator = CustomGeneratedKey.Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CustomGeneratedKey {
    String prefix() default "";

    public static class Generator implements DynamoDBAutoGenerator<String> {
        private final String prefix;

        public Generator(final Class<String> targetType, final CustomGeneratedKey annotation) {
            this.prefix = annotation.prefix();
        }

        public Generator() { //<- required if annotating directly
            this.prefix = "";
        }

        @Override
        public DynamoDBAutoGenerateStrategy getGenerateStrategy() {
            return DynamoDBAutoGenerateStrategy.CREATE;
        }

        @Override
        public final String generate(final String currentValue) {
            return prefix + UUID.randomUUID().toString();
        }
    }
}
