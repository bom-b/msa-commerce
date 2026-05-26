package com.msa.auth.repository;

import com.msa.auth.domain.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 예치금 엔티티에 대한 데이터 접근 인터페이스.
 */
public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {

    /**
     * 사용자 ID로 예치금 정보를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 예치금 정보, 없으면 빈 Optional
     */
    Optional<UserBalance> findByUser_Id(Long userId);
}
