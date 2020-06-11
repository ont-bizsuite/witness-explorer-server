package io.ont.utils;

import java.util.regex.Pattern;

/**
 * 常量
 */
public class Constant {

    public static final String LOGIN_CALLBACK_URI = "admin/login/callback";

    /**
     * sdk手续费
     */
    public static final long GAS_PRICE = 500;
    public static final long GAS_LIMIT = 2000000;

    /**
     * Token 类型
     */
    public static final String ACCESS_TOKEN = "access_token";

    public static final Integer ACTIVE = 1;


    /**
     * token 过期时间
     */
    public static final long ACCESS_TOKEN_EXPIRE = 24 * 60 * 60 * 1000;

}
