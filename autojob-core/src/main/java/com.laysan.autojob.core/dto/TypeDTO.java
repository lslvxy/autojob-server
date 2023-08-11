package com.laysan.autojob.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TypeDTO {
    private String type;
    private String name;
    private String icon;
    private String status = "OK";

    public TypeDTO(String type, String name, String icon) {
        this.type = type;
        this.name = name;
        this.icon = icon;
    }
}
