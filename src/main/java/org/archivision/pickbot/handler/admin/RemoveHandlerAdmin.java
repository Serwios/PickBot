package org.archivision.pickbot.handler.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.handler.SubCommandProcessor;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.service.PlaceService;
import org.archivision.pickbot.service.RoundService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RemoveHandlerAdmin implements AdminCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;

    private final PlaceService placeService;
    private final RoundService roundService;

    private final Map<String, SubCommandProcessor> subCommandHandlers = Map.of(
            "all", this::handleRemoveAll,
            "round", this::handleRemoveRound,
            "place", this::handleRemovePlace
    );

    @Override
    @Transactional
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /remove <round|place|all> [параметри]");
        }

        final String subCommand = args[1];
        final SubCommandProcessor subCommandProcessor = subCommandHandlers.get(subCommand);
        if (subCommandProcessor != null) {
            return subCommandProcessor.process(update, args, chatId);
        }

        return BotResponse.of(update.getMessage().getChatId(), "Неправильна команда");
    }

    @Override
    public String getCommandName() {
        return Command.REMOVE.getName();
    }

    private BotResponse handleRemoveAll(Update update, String[] args, Long chatId) {
        roundRepository.deleteAllByChatId(chatId);
        placeRepository.deleteAllByChatId(chatId);
        return BotResponse.of(update.getMessage().getChatId(), "Всі раунди і місця видалено");
    }

    private BotResponse handleRemoveRound(Update update, String[] args, Long chatId) {
        if (args.length < 3) {
            return BotResponse.of(update.getMessage().getChatId(), "Вкажіть індекс або назву раунду");
        }

        final String roundIdentifier = args[2];
        final Optional<Round> round = roundService.findRoundByIdentifier(chatId, roundIdentifier);
        if (round.isPresent()) {
            placeRepository.deleteByRoundId(round.get().getId());
            roundRepository.delete(round.get());
            return BotResponse.of(update.getMessage().getChatId(), "Раунд '" + round.get().getName() + "' і його місця видалено");
        } else {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено");
        }
    }

    private BotResponse handleRemovePlace(Update update, String[] args, Long chatId) {
        if (args.length < 4) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /remove place <індекс_раунду_або_назва> <індекс_місця_або_назва>");
        }

        final String roundIdentifier = args[2];
        final String placeIdentifier = args[3];

        final Optional<Round> round = roundService.findRoundByIdentifier(chatId, roundIdentifier);
        if (round.isPresent()) {
            final Optional<Place> place = placeService.findPlaceByIdentifier(round.get().getId(), placeIdentifier);
            if (place.isPresent()) {
                placeRepository.delete(place.get());
                return BotResponse.of(update.getMessage().getChatId(), "Місце '" + place.get().getName() + "' видалено");
            } else {
                return BotResponse.of(update.getMessage().getChatId(), "Місце не знайдено");
            }
        } else {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено");
        }
    }
}
