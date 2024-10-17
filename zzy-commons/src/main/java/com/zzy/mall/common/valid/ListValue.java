package com.zzy.mall.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义校验注解
 */
@Documented
@Constraint(
        validatedBy = {ListValueConstrainValidator.class}
)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {

    String message() default "提交的数据必须在数据列表中";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] val() default {};

}
