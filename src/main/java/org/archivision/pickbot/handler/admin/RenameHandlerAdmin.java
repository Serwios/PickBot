package org.archivision.pickbot.handler.admin;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.util.LevenshteinComparator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
@RequiredArgsConstructor
public class RenameHandlerAdmin implements AdminCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;
    private final LevenshteinComparator levenshteinComparator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 3) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /rename <round|place> [параметри]");
        }

        String entityType = args[1];
        if ("round".equalsIgnoreCase(entityType)) {
            return handleRenameRound(update, args, chatId);
        } else if ("place".equalsIgnoreCase(entityType)) {
            return handleRenamePlace(update, args, chatId);
        } else {
            return BotResponse.of(update.getMessage().getChatId(), "Неправильний тип об'єкта для перейменування.");
        }
    }

    private BotResponse handleRenameRound(Update update, String[] args, Long chatId) {
        if (args.length < 4) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /rename round <індекс_раунду_або_назва> <нова_назва>");
        }

        String roundIdentifier = args[2];
        String newName = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim();

        if (newName.isEmpty() || isNumeric(newName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Нова назва не може бути порожньою або містити лише числа.");
        }

        Optional<Round> round = isNumeric(roundIdentifier) ?
                findRoundByIndex(chatId, Integer.parseInt(roundIdentifier) - 1) :
                roundRepository.findByChatIdAndName(chatId, roundIdentifier);

        if (round.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено.");
        }

        List<Round> existingRounds = roundRepository.findByChatId(chatId);
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

        String roundIdentifier = args[2];
        String placeIdentifier = args[3];
        String newName = String.join(" ", Arrays.copyOfRange(args, 4, args.length)).trim();

        if (newName.isEmpty() || isNumeric(newName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Нова назва не може бути порожньою або містити лише числа.");
        }

        Optional<Round> round = isNumeric(roundIdentifier) ?
                findRoundByIndex(chatId, Integer.parseInt(roundIdentifier) - 1) :
                roundRepository.findByChatIdAndName(chatId, roundIdentifier);

        if (round.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Раунд не знайдено.");
        }

        Optional<Place> place = isNumeric(placeIdentifier) ?
                findPlaceByIndex(round.get().getId(), Integer.parseInt(placeIdentifier) - 1) :
                placeRepository.findByRoundIdAndName(round.get().getId(), placeIdentifier);

        if (place.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Місце не знайдено.");
        }

        List<Place> existingPlaces = placeRepository.findByRoundId(round.get().getId());
        for (Place existingPlace : existingPlaces) {
            if (!existingPlace.getId().equals(place.get().getId()) &&
                    levenshteinComparator.compare(existingPlace.getName().trim(), newName)) {
                return BotResponse.of(update.getMessage().getChatId(), "Місце з такою назвою вже існує.");
            }
        }

        place.get().setName(newName);
        placeRepository.save(place.get());

        return BotResponse.of(update.getMessage().getChatId(), "Місце перейменовано на '" + newName + "'.");
    }

    private Optional<Round> findRoundByIndex(Long chatId, int index) {
        List<Round> rounds = roundRepository.findByChatId(chatId);
        return (index >= 0 && index < rounds.size()) ? Optional.of(rounds.get(index)) : Optional.empty();
    }

    private Optional<Place> findPlaceByIndex(Long roundId, int index) {
        List<Place> places = placeRepository.findByRoundId(roundId);
        return (index >= 0 && index < places.size()) ? Optional.of(places.get(index)) : Optional.empty();
    }

    @Override
    public String getCommandName() {
        return Command.RENAME.getName();
    }
}
