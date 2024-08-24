package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.util.LevenshteinComparator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
@RequiredArgsConstructor
public class StartRoundHandlerUser implements UserCommandHandler {
    private static final int MAX_ROUNDS_PER_CHAT = 32;

    private final RoundRepository roundRepository;
    private final LevenshteinComparator levenshteinComparator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /start <назва_раунду>");
        }

        final String roundName = args[1].trim();

        if (roundName.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не може бути порожнім або складатися лише з пробілів");
        }

        if (isNumeric(roundName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не може складатися лише з чисел");
        }

        final List<Round> existingRounds = roundRepository.findByChatId(chatId);
        if (existingRounds.size() >= MAX_ROUNDS_PER_CHAT) {
            return BotResponse.of(update.getMessage().getChatId(), "Неможливо створити більше ніж " + MAX_ROUNDS_PER_CHAT + " раунди в одному чаті");
        }

        for (Round existingRound : existingRounds) {
            String existingRoundName = existingRound.getName().trim();
            if (levenshteinComparator.compare(existingRoundName, roundName)) {
                return BotResponse.of(update.getMessage().getChatId(), "Раунд з подібною назвою '" + roundName + "' вже існує");
            }
        }

        final Round activeRound = roundRepository.findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status.ACTIVE, chatId);
        if (activeRound != null) {
            return BotResponse.of(update.getMessage().getChatId(), "Не можна розпочати новий раунд, поки попередній не завершено.");
        }

        final Round round = new Round();
        round.setName(roundName);
        round.setChatId(chatId);
        round.setStartedBy(update.getMessage().getFrom().getId());
        round.setStatus(Round.Status.ACTIVE);

        roundRepository.save(round);

        return BotResponse.of(update.getMessage().getChatId(), "Раунд '" + roundName + "' розпочався!");
    }

    @Override
    public String getCommandName() {
        return Command.START_ROUND.getName();
    }
}
