package com.laysan.autojob.core.entity;

import com.laysan.autojob.core.base.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lise
 * @version CloudAccount.java, v 0.1 2020年11月27日 17:26 lise
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "autojob_menu")
public class Menu extends BaseEntity {
    private String type;
    private String name;
    private String icon;
    private String status = "OK";
}