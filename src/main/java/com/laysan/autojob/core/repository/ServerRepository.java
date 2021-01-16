package com.laysan.autojob.core.repository;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    Server findByUserId(String userId);
}
