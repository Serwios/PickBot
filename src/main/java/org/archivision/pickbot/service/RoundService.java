package org.archivision.pickbot.service;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.repo.RoundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.archivision.pickbot.service.Util.isWholeStringNumeric;

@Service
@RequiredArgsConstructor
public class RoundService {
    private final RoundRepository roundRepository;

    public Optional<Round> findRoundByIdentifier(Long chatId, String identifier) {
        if (isWholeStringNumeric(identifier)) {
            int ordinalIndex = Integer.parseInt(identifier) - 1;
            List<Round> rounds = roundRepository.findByChatId(chatId);
            if (ordinalIndex >= 0 && ordinalIndex < rounds.size()) {
                return Optional.of(rounds.get(ordinalIndex));
            }
        }
        return roundRepository.findByChatIdAndName(chatId, identifier);
    }

    public Optional<Round> findRoundByIndex(Long chatId, int index) {
        final List<Round> rounds = roundRepository.findByChatId(chatId);
        return (index >= 0 && index < rounds.size()) ? Optional.of(rounds.get(index)) : Optional.empty();
    }
}
