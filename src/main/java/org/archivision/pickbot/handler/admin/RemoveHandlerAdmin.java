package org.archivision.pickbot.handler.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RemoveHandlerAdmin implements AdminCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;

    @Override
    @Transactional
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /remove <round|place|all> [параметри]");
        }

        String subCommand = args[1];
        if ("all".equalsIgnoreCase(subCommand)) {
            roundRepository.deleteAllByChatId(chatId);
            placeRepository.deleteAllByChatId(chatId);
            return BotResponse.of(update.getMessage().getChatId(), "Всі раунди і місця видалено");
        } else if ("round".equalsIgnoreCase(subCommand)) {
            return handleRemoveRound(update, args, chatId);
        } else if ("place".equalsIgnoreCase(subCommand)) {
            return handleRemovePlace(update, args, chatId);
        } else {
            return BotResponse.of(update.getMessage().getChatId(), "Неправильна команда");
        }
    }

    private BotResponse handleRemoveRound(Update update, String[] args, Long chatId) {
        if (args.length < 3) {
            return BotResponse.of(update.getMessage().getChatId(), "Вкажіть індекс або назву раунду.");
        }

        String roundIdentifier = args[2];
        Optional<Round> round = findRoundByIdentifier(chatId, roundIdentifier);
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

        String roundIdentifier = args[2];
        String placeIdentifier = args[3];

        Optional<Round> round = findRoundByIdentifier(chatId, roundIdentifier);
        if (round.isPresent()) {
            Optional<Place> place = findPlaceByIdentifier(round.get().getId(), placeIdentifier);
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

    private Optional<Place> findPlaceByIdentifier(Long roundId, String identifier) {
        if (isNumeric(identifier)) {
            int ordinalIndex = Integer.parseInt(identifier) - 1;
            List<Place> places = placeRepository.findByRoundId(roundId);
            if (ordinalIndex >= 0 && ordinalIndex < places.size()) {
                return Optional.of(places.get(ordinalIndex));
            }
        }
        return placeRepository.findByRoundIdAndName(roundId, identifier);
    }

    @Override
    public String getCommandName() {
        return Command.REMOVE.getName();
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
