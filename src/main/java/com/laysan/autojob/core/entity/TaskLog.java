package com.laysan.autojob.core.entity;

import cn.hutool.core.util.StrUtil;
import com.laysan.autojob.core.base.BaseEntity;
import com.laysan.autojob.core.constants.AccountType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
@NoArgsConstructor
public class TaskLog extends BaseEntity {
    private Long userId;
    private Long accountId;
    private String account;
    private String type;
    @Transient
    private String typeName;

    private Integer succeed;
    @Column(nullable = false, columnDefinition = "text")
    private String detail;
    private Long timeCosted;
    private String executedDay;

    public TaskLog(Long userId) {
        this.userId = userId;
    }

    public void setDetail(String detail) {
        if (!Objects.isNull(detail) && detail.length() > 200) {
            this.detail = detail.substring(0, 200);
        } else {
            this.detail = detail;
        }
    }

    public String getTypeName() {
        if (StrUtil.isBlank(type)) {
            return StrUtil.EMPTY;
        }
        return AccountType.get(this.getType()).getDesc();
    }
}
