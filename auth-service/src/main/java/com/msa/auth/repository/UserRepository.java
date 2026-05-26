package com.msa.auth.repository;

import com.msa.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 엔티티에 대한 데이터 접근 인터페이스.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자 아이디로 사용자를 조회한다.
     *
     * @param username 조회할 사용자 아이디
     * @return 해당 아이디의 사용자, 없으면 빈 Optional
     */
    Optional<User> findByUsername(String username);
}
