package com.laysan.autojob.core.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @author lise
 * @version CloudAccount.java, v 0.1 2020年11月27日 17:26 lise
 */
@Data
@Entity
@Table
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Date gmtCreate;

    @UpdateTimestamp
    private Date gmtModified;

    /**
     * openid
     */
    @NotBlank(message = "OpenID 不能为空")
    private String userId;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Length(max = 15, min = 8, message = "账号格式不正确")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 时间
     */
    @NotBlank(message = "执行时间不能为空")
    private String time;

    /**
     * 类型 @see ModuleTypes
     */
    @NotBlank(message = "类型不能为空")
    private String type;

}