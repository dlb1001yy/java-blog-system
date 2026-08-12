package com.dlbyy.blog.utils;

import java.util.regex.Pattern;

/**
 * 密码强度校验器
 * 规则：至少 8 位，且同时包含大写字母、小写字母、数字、特殊字符。
 */
public class PasswordStrengthValidator {

    private static final int MIN_LENGTH = 8;

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    /**
     * 校验密码是否满足强度规则
     *
     * @param password 明文密码
     * @return 校验结果，{@link ValidationResult#valid()} 为 true 表示通过
     */
    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("密码不能为空");
        }
        if (password.length() < MIN_LENGTH) {
            return ValidationResult.invalid("密码长度至少 8 位");
        }
        if (!UPPER.matcher(password).find()) {
            return ValidationResult.invalid("密码必须包含大写字母");
        }
        if (!LOWER.matcher(password).find()) {
            return ValidationResult.invalid("密码必须包含小写字母");
        }
        if (!DIGIT.matcher(password).find()) {
            return ValidationResult.invalid("密码必须包含数字");
        }
        if (!SPECIAL.matcher(password).find()) {
            return ValidationResult.invalid("密码必须包含特殊字符（如 !@#$%^&* 等）");
        }
        return ValidationResult.valid();
    }

    /**
     * 是否通过强度校验
     */
    public static boolean isValid(String password) {
        return validate(password).isValid();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "密码强度满足要求");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
