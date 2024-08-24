package org.archivision.pickbot.repo;

import org.archivision.pickbot.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    Round findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status status, Long chatId);
    Optional<Round> findByIdAndChatId(Long id, Long chatId);
    List<Round> findByChatId(Long chatId);
    Optional<Round> findByChatIdAndName(Long chatId, String name);

    void deleteAllByChatId(Long chatId);
}
