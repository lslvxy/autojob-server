package com.laysan.autojob.core.entity;

import com.laysan.autojob.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@Table(name = "autojob_user")
public class User extends BaseEntity {
    /**
     * 用户第三方系统的唯一id。在调用方集成该组件时，可以用uuid + source唯一确定一个用户
     */
    @Column(unique = true, nullable = false)
    private String openId;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 今日已执行定时任务数量
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer todayRunCount;
    /**
     * 用户总账号数量
     */
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer totalAccountCount;

    /**
     * sct.ftqq.com Server酱key
     */
    private String sctKey;


    /**
     * 今日是否已全部完成
     *
     * @return
     */
    public boolean todayCompleted() {
        return todayRunCount.compareTo(totalAccountCount) >= 0;
    }
}