package org.archivision.pickbot.repo;

import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserIdAndPlaceAndChatId(Long userId, Place place, Long chatId);
}
