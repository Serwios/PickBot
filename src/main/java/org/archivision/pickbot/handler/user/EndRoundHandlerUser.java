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

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EndRoundHandlerUser implements UserCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;
    private final TemplateGenerator templateGenerator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        final Round activeRound = roundRepository.findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status.ACTIVE, chatId);
        if (activeRound == null) {
            return BotResponse.of(update.getMessage().getChatId(), "Немає активного раунду");
        }

        activeRound.setStatus(Round.Status.ENDED);
        activeRound.setEndedBy(update.getMessage().getFrom().getId());
        activeRound.setEndedAt(LocalDateTime.now());

        roundRepository.save(activeRound);

        return BotResponse.of(update.getMessage().getChatId(),
                "Раунд успішно завершився! \n" +
                        templateGenerator.parsePlacesToResponseTemplate(placeRepository.findByRoundOrderByVotesDesc(activeRound))
        );
    }

    @Override
    public String getCommandName() {
        return Command.END_ROUND.getName();
    }
}
