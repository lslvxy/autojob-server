package com.laysan.autojob.core.entity;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laysan.autojob.core.base.BaseEntity;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.utils.JobUtils;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lise
 * @version CloudAccount.java, v 0.1 2020年11月27日 17:26 lise
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "autojob_account")
public class Account extends BaseEntity {
    @Column(nullable = false)
    private Long   userId;
    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    /**
     * 类型 @see ModuleTypes
     */
    @NotBlank(message = "类型不能为空")
    @Column(nullable = false)
    private String type;
    @Transient
    private String typeName;
    @Transient
    private String typeIcon;

    /**
     * 时间
     */
    @NotBlank(message = "执行时间不能为空")
    @Column(nullable = false, columnDefinition = "varchar(5) default '00:00'")
    private String  time;
    /**
     * 上次执行时间
     */
    private Date    lastRunTime;
    /**
     * 任务状态 1启动 0暂停
     */
    private Integer status;
    @JsonIgnore
    @Column(columnDefinition = "text")
    private String  extendInfo;

    @Column(nullable = false, columnDefinition = "int default -1")
    private Integer todayExecuted;

    public Account(Long userId) {
        this.setUserId(userId);
    }

    public Account(Long userId, String type) {
        this.setUserId(userId);
        this.setType(type);
    }

    public String buildJobName() {
        return JobUtils.buildJobName(this);
    }

    public String buildCronExpression() {
        return JobUtils.buildCron(this);
    }

    public String buildJobClass() {
        return "";
    }

    public String getTypeName() {
        if (StrUtil.isBlank(type)) {
            return StrUtil.EMPTY;
        }
        return AccountType.get(this.getType()).getDesc();
    }

    public String getTypeIcon() {
        if (StrUtil.isBlank(type)) {
            return StrUtil.EMPTY;
        }
        return AccountType.get(this.getType()).getIcon();
    }

    public JSONObject buildExtendInfo() {
        if (StrUtil.isBlank(extendInfo)) {
            return new JSONObject();
        }
        return JSON.parseObject(extendInfo);
    }

    public void saveExtendInfo(JSONObject jsonObject) {
        this.setExtendInfo(jsonObject.toJSONString());
    }

}