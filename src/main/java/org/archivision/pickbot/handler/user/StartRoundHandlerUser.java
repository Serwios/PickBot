package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.service.LevenshteinComparator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.archivision.pickbot.service.Util.toCamelCase;

@Component
@RequiredArgsConstructor
public class StartRoundHandlerUser implements UserCommandHandler {
    private static final int MAX_ROUNDS_PER_CHAT = 32;

    private final RoundRepository roundRepository;
    private final LevenshteinComparator levenshteinComparator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /start <назва_раунду> [тайм_аут_в_годинах]");
        }

        String roundName = toCamelCase(String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim());
        if (roundName.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не може бути порожнім або складатися лише з пробілів");
        }

        Integer timeoutInHours = null;
        if (isNumeric(args[args.length - 1])) {
            try {
                timeoutInHours = Integer.parseInt(args[args.length - 1]);
                roundName = toCamelCase(String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)).trim());
            } catch (NumberFormatException e) {
                return BotResponse.of(update.getMessage().getChatId(), "Часовий інтервал повинен бути числом");
            }
        }

        if (timeoutInHours != null && timeoutInHours <= 0) {
            return BotResponse.of(update.getMessage().getChatId(), "Часовий інтервал повинен бути більше нуля");
        }

        if (isNumeric(roundName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не може складатися лише з чисел");
        }

        final List<Round> existingRounds = roundRepository.findByChatId(chatId);
        if (existingRounds.size() >= MAX_ROUNDS_PER_CHAT) {
            return BotResponse.of(update.getMessage().getChatId(), "Неможливо створити більше ніж " + MAX_ROUNDS_PER_CHAT + " раунди в одному чаті");
        }

        for (Round existingRound : existingRounds) {
            String existingRoundName = toCamelCase(existingRound.getName().trim());
            if (levenshteinComparator.compare(existingRoundName, roundName)) {
                return BotResponse.of(update.getMessage().getChatId(), "Раунд з подібною назвою '" + roundName + "' вже існує");
            }
        }

        final Round activeRound = roundRepository.findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status.ACTIVE, chatId);
        if (activeRound != null) {
            return BotResponse.of(update.getMessage().getChatId(), "Не можна розпочати новий раунд, поки попередній не завершено");
        }

        final Round round = new Round();
        round.setName(roundName);
        round.setChatId(chatId);
        round.setStartedBy(update.getMessage().getFrom().getId());
        round.setStatus(Round.Status.ACTIVE);

        if (timeoutInHours != null) {
            round.setEndAt(LocalDateTime.now().plusHours(timeoutInHours));
        }

        roundRepository.save(round);

        final String timeOutResponse = timeoutInHours == null ? "" : "\nТайм-аут в годинах: " + timeoutInHours;

        return BotResponse.of(update.getMessage().getChatId(), "Раунд '" + roundName + "' розпочався!" + timeOutResponse);
    }

    @Override
    public String getCommandName() {
        return Command.START_ROUND.getName();
    }
}
