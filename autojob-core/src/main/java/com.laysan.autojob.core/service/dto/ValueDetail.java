package com.laysan.autojob.core.service.dto;

import lombok.Data;

/**
 * @author lise
 * @version ValueDetail.java, v 0.1 2020年11月26日 14:38 lise
 */
@Data
public class ValueDetail {
    private Object value;

    public ValueDetail(Object value) {
        this.value = value;
    }
}