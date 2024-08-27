package org.archivision.pickbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.archivision.pickbot.bot.VotingBot;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.repo.RoundRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundExpiryService {
    private final RoundRepository roundRepository;
    private final VotingBot votingBot;

    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkAndEndExpiredRounds() {
        doCheck();
    }

    private void doCheck() {
        log.info("Checking expired rounds...");

        final List<Round> byStatusAndEndAtBefore = roundRepository.findByStatusAndEndAtBefore(Round.Status.ACTIVE, LocalDateTime.now());
        log.info("Found {} expired rounds", byStatusAndEndAtBefore.size());
        for (Round round : byStatusAndEndAtBefore) {
            round.setStatus(Round.Status.ENDED);
            roundRepository.save(round);
            votingBot.sendMsg(BotResponse.of(round.getChatId(), "Раунд " + round.getName() + " завершений по тайм-ауту"));
        }
    }
}
