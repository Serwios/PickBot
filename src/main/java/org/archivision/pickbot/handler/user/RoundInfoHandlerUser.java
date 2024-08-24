package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.util.TemplateGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoundInfoHandlerUser implements UserCommandHandler {
    private final PlaceRepository placeRepository;
    private final RoundRepository roundRepository;
    private final TemplateGenerator templateGenerator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /round <індекс_раунду_або_назва>");
        }

        String roundIdentifier = args[1];
        Optional<Round> roundOpt = findRoundByIdentifier(chatId, roundIdentifier);

        return roundOpt
                .map(round -> BotResponse.of(
                        update.getMessage().getChatId(),
                        templateGenerator.parsePlacesToResponseTemplate(
                                placeRepository.findByRoundOrderByVotesDesc(round)
                        )
                ))
                .orElseGet(() -> BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдений"));
    }

    private Optional<Round> findRoundByIdentifier(Long chatId, String identifier) {
        if (isNumeric(identifier)) {
            int ordinalIndex = Integer.parseInt(identifier) - 1;
            List<Round> rounds = roundRepository.findByChatId(chatId);
            if (ordinalIndex >= 0 && ordinalIndex < rounds.size()) {
                return Optional.of(rounds.get(ordinalIndex));
            }
        }
        return roundRepository.findByChatIdAndName(chatId, identifier);
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getCommandName() {
        return Command.ROUND.getName();
    }
}
