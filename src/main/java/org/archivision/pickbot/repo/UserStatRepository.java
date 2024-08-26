package org.archivision.pickbot.repo;

import org.archivision.pickbot.entity.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserStatRepository extends JpaRepository<UserStat, Long> {
    Optional<UserStat> findByUserId(Long userId);
    long countByLastInteractionDateAfter(LocalDateTime threshold);
}

