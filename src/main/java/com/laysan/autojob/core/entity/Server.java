package com.laysan.autojob.core.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class Server {
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
     * sckey
     */
    private String sckey;

}