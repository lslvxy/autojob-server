package com.laysan.autojob.core.repository;

import com.laysan.autojob.core.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {
}
