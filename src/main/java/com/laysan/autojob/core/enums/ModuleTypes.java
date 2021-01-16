package com.laysan.autojob.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModuleTypes {

    EVERPHOTO("everphoto", "时光相册"),

    CLOUD189("cloud189", "天翼云盘");

    private String name;
    private String description;
}
