package com.laysan.autojob.core.repository;

import com.laysan.autojob.core.entity.AuthKeyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthKeyConfigRepository extends JpaRepository<AuthKeyConfig, Long> {

    AuthKeyConfig findBySource(String source);


}
