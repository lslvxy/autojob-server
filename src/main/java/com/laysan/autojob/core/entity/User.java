package com.laysan.autojob.core.entity;

import com.laysan.autojob.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhyd.oauth.model.AuthUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author lise
 * @version CloudAccount.java, v 0.1 2020年11月27日 17:26 lise
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class User extends BaseEntity {
    /**
     * 用户第三方系统的唯一id。在调用方集成该组件时，可以用uuid + source唯一确定一个用户
     */
    @Column(unique = true, nullable = false)
    private String uuid;
    /**
     * 账号来源 github/wx/qq eq.
     */
    @Column(nullable = false)
    private String source;
    /**
     * 账户
     */
    private String username;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 头像
     */
    private String avatar;

    /**
     * accessToken
     */
    private String accessToken;

    public static User fromAuthUser(AuthUser authUser) {
        User user = new User();
        user.setUuid(authUser.getUuid());
        user.setSource(authUser.getSource());
        user.setUsername(authUser.getUsername());
        user.setNickname(authUser.getNickname());
        user.setAvatar(authUser.getAvatar());
        user.setAccessToken(authUser.getToken().getAccessToken());
        return user;
    }
}