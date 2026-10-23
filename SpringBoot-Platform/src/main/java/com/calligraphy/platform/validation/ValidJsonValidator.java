package com.calligraphy.platform.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link ValidJson} 的校验器。
 *
 * <p>用 Jackson 解析一遍：能解析出来就是合法 JSON。
 * 打开 FAIL_ON_TRAILING_TOKENS，否则 {@code {"a":1} 这段垃圾} 会被当成合法
 * （readTree 只读第一个值，剩下的直接丢掉）。
 *
 * <p>null 放行（契约里 null 表示「不改动」）；空串不算合法 JSON。
 */
public class ValidJsonValidator implements ConstraintValidator<ValidJson, String> {

    /** ObjectMapper 是线程安全的，一个静态实例就够。 */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            MAPPER.readTree(value);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
