package com.laysan.autojob.core.repository;

import com.laysan.autojob.core.entity.Account;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    List<Account> findByTime(String time);

    @Query(" from Account where todayExecuted=:todayExecuted and time <=:time")
    List<Account> findToRun(Integer todayExecuted, String time, Pageable pageable);

    List<Account> findByUserIdAndType(String userId, String type);

    Account findByUserIdAndTypeAndAccount(String userId, String type, String account);
}
