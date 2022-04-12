package com.laysan.autojob.core.entity;

import com.laysan.autojob.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table
public class TaskLog extends BaseEntity {
    private Long userId;
    private Long accountId;
    private String account;
    private String type;
    private boolean succeed;
    @Column(nullable = false, columnDefinition = "text")
    private String detail;

    public void setDetail(String detail) {
        if (!Objects.isNull(detail) && detail.length() > 200) {
            this.detail = detail.substring(0, 200);
        } else {
            this.detail = detail;
        }
    }

}
