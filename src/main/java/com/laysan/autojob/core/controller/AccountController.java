package com.laysan.autojob.core.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.Assert;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.dto.AccountDTO;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.utils.QuartzUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class AccountController extends BaseController {

    @Autowired
    AutoRunService autoRunService;
    @Resource
    private AccountRepository accountRepository;
    @Resource
    private AccountService accountService;
    @Resource
    private Scheduler scheduler;

    @GetMapping("/account")
    public PageResponse<Account> list(int current, int pageSize, HttpServletRequest request) {
        Page<Account> accountPage = accountService.findAccountPage(getLoginUserId(request), getPageRequest(current, pageSize));
        return PageResponse.of(accountPage.getContent(), Math.toIntExact(accountPage.getTotalElements()), pageSize, current);
    }

    @PostMapping("/account")
    public Response save(@RequestBody Account account, HttpServletRequest request) throws Exception {
        Long userId = getLoginUserId(request);
        String typeDesc = AccountType.get(account.getType()).getDesc();
        long accountCountByType = accountService.findAccountCountByType(userId, account.getType());
        if (accountCountByType >= 3) {
            return PageResponse.buildFailure("500", "[" + typeDesc + "]账号数量超过限制[3]");
        }
        if (accountService.accountExistByType(userId, account.getType())) {
            return PageResponse.buildFailure("500", "[" + typeDesc + "]账号[" + account.getAccount() + "]已存在");
        }
        account.setUserId(userId);
        account.setTodayExecuted(0);
        account.setStatus(1);
        if (account.getTime().length() > 5) {
            account.setTime(CharSequenceUtil.subBefore(account.getTime(), ":", true));
        }
        QuartzUtils.createScheduleJob(scheduler, accountService.save(account));
        return PageResponse.buildSuccess();
    }

    @GetMapping("/types")
    public MultiResponse types() {
        AccountType[] values = AccountType.values();
        List<Map<String, String>> result = new ArrayList<>();
        for (AccountType value : values) {
            Map<String, String> map = new HashMap<>();
            map.put("key", value.getCode());
            map.put("value", value.getCode());
            map.put("label", value.getDesc());
            result.add(map);
        }
        return MultiResponse.of(result);
    }

    @PutMapping("/account")
    public Response update(@RequestBody Account account, HttpServletRequest request) {
        Assert.notNull(account.getId(), "id不能为空");
        Account formDb = accountService.findById(account.getId());
        Assert.isTrue(formDb.getUserId().equals(getLoginUserId(request)), "没有权限");
        formDb.setTime(account.getTime());
        accountService.save(formDb);
        return PageResponse.buildSuccess();
    }

    @DeleteMapping("/account/{id}")
    public Response delete(@PathVariable("id") Long id, HttpServletRequest request) {
        Assert.notNull(id, "id不能为空");
        accountService.deleteById(id);
        return PageResponse.buildSuccess();
    }

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
