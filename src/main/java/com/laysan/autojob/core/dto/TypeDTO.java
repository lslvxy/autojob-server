package com.laysan.autojob.core.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TypeDTO {
    private String type;
    private String name;
    private String icon;
    private String status = "ok";
}
