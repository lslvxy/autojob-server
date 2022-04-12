package com.laysan.autojob.core.repository;

import com.laysan.autojob.core.entity.UserOauth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOauthRepository extends JpaRepository<UserOauth, Long> {
    UserOauth findByUserId(Long userId);

}
