package com.laysan.autojob.core.entity;

import com.laysan.autojob.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class AuthKeyConfig extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String source;
    @Column(nullable = false)
    private String clientId;
    @Column(nullable = false)
    private String clientSecret;
    @Column(nullable = false)
    private String redirectUri;
    private String scopes;
    private String extendConfig;

}
