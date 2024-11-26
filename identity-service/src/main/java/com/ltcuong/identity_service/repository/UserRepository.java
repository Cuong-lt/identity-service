package com.ltcuong.identity_service.repository;

import com.ltcuong.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    boolean existsByUsername(String username);

    // JPA tự generate code cho phương thức tìm entity.
    // tìm entity theo trường username
    Optional<User> findByUsername(String username);
}
