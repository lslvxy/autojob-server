package com.laysan.autojob.core.dto;

import com.laysan.autojob.core.entity.Account;
import lombok.Data;

import java.util.List;

@Data
public class AccountDTO {
    private String        type;
    private String        userId;
    private List<Account> accountList;
}
