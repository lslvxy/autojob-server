package com.laysan.autojob.core.controller;

import com.laysan.autojob.core.dto.AccountDTO;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.utils.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("account")
public class AccountController extends BaseController {

    @Autowired
    AutoRunService autoRunService;
    @Resource
    private AccountRepository accountRepository;

    /**
     * 保存账号
     *
     * @param accountDTO
     * @return
     */
    @PostMapping("/create")
    @ResponseBody
    public String create(@Validated @RequestBody AccountDTO accountDTO) {
        try {
            String userId = accountDTO.getUserId();
            String type = accountDTO.getType();
            for (Account account : accountDTO.getAccountList()) {
                Account accountQuery;
//                accountQuery = accountRepository.findByUserIdAndTypeAndAccount(userId, type,
//                        account.getAccount());
//                if (!Objects.isNull(accountQuery)) {
//                    account.setId(accountQuery.getId());
//                }
//
//                if (Objects.equals(account.getPassword(), "****")) {
//                    account.setPassword(accountQuery.getPassword());
//                } else {
//                    account.setPassword(AESUtil.encrypt(account.getPassword()));
//                }
//                account.setUserId(userId);
//                account.setType(type);

                accountRepository.save(account);

                autoRunService.run(account);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return "配置失败";
        }
        return "配置成功";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestBody Account dto) {
        try {
            Optional<Account> account = accountRepository.findById(dto.getId());
            account.ifPresent(value -> accountRepository.delete(value));

        } catch (Exception e) {
            e.printStackTrace();
            return "删除失败";
        }
        return "删除成功";
    }

    @PostMapping("/get")
    @ResponseBody
    public Object getDetail(@RequestBody Account dto) {
        List<Account> accountList = accountRepository.findByUserIdAndType(null, dto.getType());

        return accountList.stream().map(v -> {
            Map<String, String> result = new HashMap<>();
            result.put("_id", UUID.randomUUID().toString());
            result.put("id", String.valueOf(v.getId()));
            result.put("account", v.getAccount());
            result.put("password", "****");
            result.put("time", v.getTime());
            return result;
        }).collect(Collectors.toList());
    }
}
