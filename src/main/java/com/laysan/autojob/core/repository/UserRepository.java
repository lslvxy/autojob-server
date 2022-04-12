package com.laysan.autojob.core.repository;

import com.laysan.autojob.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByAccessToken(String accessToken);

    User findByUuidAndSource(String uuid, String source);
}
