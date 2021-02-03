package com.laysan.autojob.quartz.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table
public class QuartzBean {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Date gmtCreate;

    @UpdateTimestamp
    private Date gmtModified;

    private String userId;
    private Long   accountId;
    private String type;
    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务执行类
     */
    private String jobClass;

    /**
     * 任务状态 启动还是暂停
     */
    private Integer status;

    /**
     * 任务运行时间表达式
     */
    private String cronExpression;
}
