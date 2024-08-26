package org.archivision.pickbot.handler.admin;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.handler.SubCommandProcessor;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.service.LevenshteinComparator;
import org.archivision.pickbot.service.PlaceService;
import org.archivision.pickbot.service.RoundService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.archivision.pickbot.service.Util.isWholeStringNumeric;

@Component
@RequiredArgsConstructor
public class RenameHandlerAdmin implements AdminCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;
    private final LevenshteinComparator levenshteinComparator;

    private final PlaceService placeService;
    private final RoundService roundService;

    private final Map<String, SubCommandProcessor> subCommandHandlers = Map.of(
            "round", this::handleRenameRound,
            "place", this::handleRenamePlace
    );

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 3) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /rename <round|place> [параметри]");
        }

        final String entityType = args[1];
        final SubCommandProcessor subCommandProcessor = subCommandHandlers.get(entityType);
        if (subCommandProcessor != null) {
            return subCommandProcessor.process(update, args, chatId);
        }

        return BotResponse.of(update.getMessage().getChatId(), "Неправильний тип об'єкта для перейменування.");
    }

    @Override
    public String getCommandName() {
        return Command.RENAME.getName();
    }

    private BotResponse handleRenameRound(Update update, String[] args, Long chatId) {
        if (args.length < 4) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /rename round <індекс_раунду_або_назва> <нова_назва>");
        }

        final String roundIdentifier = args[2];
        final String newName = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim();

        if (newName.isEmpty() || isWholeStringNumeric(newName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Нова назва не може бути порожньою або містити лише числа");
        }

        final Optional<Round> round = isWholeStringNumeric(roundIdentifier) ?
                roundService.findRoundByIndex(chatId, Integer.parseInt(roundIdentifier) - 1) :
                roundRepository.findByChatIdAndName(chatId, roundIdentifier);

        if (round.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено");
        }

        final List<Round> existingRounds = roundRepository.findByChatId(chatId);
        for (Round existingRound : existingRounds) {
            if (!existingRound.getId().equals(round.get().getId()) &&
                    levenshteinComparator.compare(existingRound.getName().trim(), newName)) {
                return BotResponse.of(update.getMessage().getChatId(), "Раунд з такою назвою вже існує.");
            }
        }

        round.get().setName(newName);
        roundRepository.save(round.get());

        return BotResponse.of(update.getMessage().getChatId(), "Раунд перейменовано на '" + newName + "'.");
    }

    private BotResponse handleRenamePlace(Update update, String[] args, Long chatId) {
        if (args.length < 5) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /rename place <індекс_раунду_або_назва> <індекс_місця_або_назва> <нова_назва>");
        }

        final String roundIdentifier = args[2];
        final String placeIdentifier = args[3];
        final String newName = String.join(" ", Arrays.copyOfRange(args, 4, args.length)).trim();

        if (newName.isEmpty() || isWholeStringNumeric(newName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Нова назва не може бути порожньою або містити лише числа");
        }

        final Optional<Round> round = isWholeStringNumeric(roundIdentifier) ?
                roundService.findRoundByIndex(chatId, Integer.parseInt(roundIdentifier) - 1) :
                roundRepository.findByChatIdAndName(chatId, roundIdentifier);

        if (round.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено");
        }

        final Optional<Place> place = isWholeStringNumeric(placeIdentifier) ?
                placeService.findPlaceByIndex(round.get().getId(), Integer.parseInt(placeIdentifier) - 1) :
                placeRepository.findByRoundIdAndName(round.get().getId(), placeIdentifier);

        if (place.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Місце не знайдено");
        }

        List<Place> existingPlaces = placeRepository.findByRoundId(round.get().getId());
        for (Place existingPlace : existingPlaces) {
            if (!existingPlace.getId().equals(place.get().getId()) &&
                    levenshteinComparator.compare(existingPlace.getName().trim(), newName)) {
                return BotResponse.of(update.getMessage().getChatId(), "Місце з такою назвою вже існує");
            }
        }

        place.get().setName(newName);
        placeRepository.save(place.get());

        return BotResponse.of(update.getMessage().getChatId(), "Місце перейменовано на '" + newName + "'");
    }
}
