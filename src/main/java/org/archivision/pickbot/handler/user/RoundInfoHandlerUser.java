package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.service.RoundService;
import org.archivision.pickbot.service.TemplateGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoundInfoHandlerUser implements UserCommandHandler {
    private final PlaceRepository placeRepository;
    private final TemplateGenerator templateGenerator;
    private final RoundService roundService;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /round <індекс_раунду_або_назва>");
        }

        final String roundIdentifier = args[1];
        final Optional<Round> roundOpt = roundService.findRoundByIdentifier(chatId, roundIdentifier);

        return roundOpt
                .map(round -> BotResponse.of(
                        update.getMessage().getChatId(),
                        templateGenerator.parsePlacesToResponseTemplate(
                                placeRepository.findByRoundOrderByVotesDesc(round)
                        )
                ))
                .orElseGet(() -> BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдений"));
    }

    @Override
    public String getCommandName() {
        return Command.ROUND.getName();
    }
}
