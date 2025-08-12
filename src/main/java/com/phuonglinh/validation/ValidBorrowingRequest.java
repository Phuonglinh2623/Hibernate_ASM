package com.phuonglinh.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BorrowingRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBorrowingRequest {
    String message() default "Invalid borrowing request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
