package com.calligraphy.platform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验一个字符串是不是**合法 JSON**。
 *
 * <p>用在 {@code users.preferences} 这类「整段存 JSON 文本」的列上：
 * 内容非法时在入参边界就报错（400），而不是把坏数据写进库里，
 * 让前端读回来才发现「设置存不进去」。
 *
 * <p>null 视为合法 —— 契约里 {@code preferences} 传 null 表示「不改动」，
 * 不该当成非法 JSON 拒掉。
 */
@Documented
@Constraint(validatedBy = ValidJsonValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidJson {

    String message() default "必须是合法的 JSON";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
