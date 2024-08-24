package org.archivision.pickbot.handler.admin;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClearHandlerAdmin implements AdminCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /clear <round|all> [назва_раунду]");
        }

        String subCommand = args[1];
        if ("all".equalsIgnoreCase(subCommand)) {
            roundRepository.deleteAllByChatId(chatId);
            placeRepository.deleteAllByChatId(chatId);
            return BotResponse.of(update.getMessage().getChatId(), "Всі раунди і місця видалено.");
        } else if ("round".equalsIgnoreCase(subCommand)) {
            if (args.length < 3) {
                return BotResponse.of(update.getMessage().getChatId(), "Вкажіть індекс або назву раунду.");
            }

            String roundIdentifier = args[2];
            Optional<Round> round = roundRepository.findByChatIdAndName(chatId, roundIdentifier);
            if (round.isPresent()) {
                placeRepository.deleteByRoundId(round.get().getId());
                roundRepository.delete(round.get());
                return BotResponse.of(update.getMessage().getChatId(), "Раунд '" + roundIdentifier + "' і його місця видалено.");
            } else {
                return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено.");
            }
        } else {
            return BotResponse.of(update.getMessage().getChatId(), "Невірна команда.");
        }
    }


    @Override
    public String getCommandName() {
        return Command.CLEAR.getName();
    }
}
