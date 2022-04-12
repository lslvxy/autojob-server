package com.laysan.autojob.core.entity;

import com.laysan.autojob.core.base.BaseEntity;
import com.laysan.autojob.core.job.AutojobTask;
import com.laysan.autojob.core.utils.JobUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quartz.JobKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * @author lise
 * @version CloudAccount.java, v 0.1 2020年11月27日 17:26 lise
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class Account extends BaseEntity {
    @Column(nullable = false)
    private Long userId;
    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;
    /**
     * 类型 @see ModuleTypes
     */
    @NotBlank(message = "类型不能为空")
    @Column(nullable = false)
    private String type;

    /**
     * 时间
     */
    @NotBlank(message = "执行时间不能为空")
    @Column(nullable = false, columnDefinition = "varchar(5) default '00:00'")
    private String time;

    /**
     * 任务状态 启动还是暂停
     */
    private Integer status;

    public String getJobName() {
        return JobUtils.buildJobName(this);
    }

    public JobKey getJobKey() {
        return JobKey.jobKey(getJobName());
    }

    public String getCronExpression() {
        return JobUtils.buildCron(this);
    }

    public String getJobClass() {
        return AutojobTask.class.getName();
    }

}