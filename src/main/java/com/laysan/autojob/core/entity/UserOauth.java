package com.laysan.autojob.core.entity;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
public class UserOauth extends BaseEntity {

    @Column(unique = true, nullable = false)
    private Long userId;
    /**
     * 账号来源 github/wx/qq eq.
     */
    private String source;
    /**
     * openid eq
     */
    private String oauthId;

    /**
     * accessToken
     */
    private String accessToken;
    private Integer expireIn;
    private String refreshToken;
    private Integer refreshTokenExpireIn;

    public static UserOauth fromAuthUser(AuthUser authUser) {
        UserOauth user = new UserOauth();
        user.setSource(authUser.getSource());
        String openId = authUser.getToken().getOpenId();
        String userId = authUser.getToken().getUserId();
        String uid = authUser.getToken().getUid();
        String unionId = authUser.getToken().getUnionId();
        String oauthId = ArrayUtil.firstMatch(StrUtil::isNotBlank, openId, userId, uid, unionId);
        user.setOauthId(oauthId);
        user.setAccessToken(authUser.getToken().getAccessToken());
        user.setExpireIn(authUser.getToken().getExpireIn());
        user.setRefreshToken(authUser.getToken().getRefreshToken());
        user.setRefreshTokenExpireIn(authUser.getToken().getRefreshTokenExpireIn());
        return user;
    }
}