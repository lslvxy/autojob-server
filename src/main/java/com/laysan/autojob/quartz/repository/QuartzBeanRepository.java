package com.laysan.autojob.quartz.repository;

import com.laysan.autojob.quartz.entity.QuartzBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuartzBeanRepository extends JpaRepository<QuartzBean, Long> {
    List<QuartzBean> findByUserId(String userId);

    QuartzBean findByAccountId(Long accountId);

    QuartzBean findByJobName(String jobName);
}
