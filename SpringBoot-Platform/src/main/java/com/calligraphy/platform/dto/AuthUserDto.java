package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 契约 AuthUser：登录、注册、资料接口暴露的用户资料。
 *
 * <p>字段名与大小写必须与 openapi/contract.yaml 的 AuthUser schema 完全一致，
 * 前端用户与管理员共用这一个形状，靠 role 区分（USER / ADMIN / SUPER_ADMIN）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {

    /** 用户 ID（users 表主键，UUID 字符串） */
    private String id;

    /** 登录名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像地址，对应 users 表的 avatar_url 列 */
    private String avatar;

    /** 角色：USER / ADMIN / SUPER_ADMIN */
    private String role;
}
