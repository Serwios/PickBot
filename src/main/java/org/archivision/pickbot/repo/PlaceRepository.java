package org.archivision.pickbot.repo;

import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByRoundOrderByVotesDesc(Round round);
    Place findByNameAndRound(String name, Round round);
    List<Place> findByRoundId(Long roundId);
    Optional<Place> findByRoundIdAndName(Long roundId, String name);

    void deleteByRoundId(Long roundId);
    void deleteAllByChatId(Long chatId);
}

