package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.service.LevenshteinComparator;
import org.archivision.pickbot.service.Util;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.archivision.pickbot.service.Util.toCamelCase;

@Component
@RequiredArgsConstructor
public class AddPlaceHandlerUser implements UserCommandHandler {
    private static final int MAX_PLACES_PER_ROUND = 32;

    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;
    private final LevenshteinComparator levenshteinComparator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /add <місце>");
        }

        final String rawInput = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final String[] placeNames = parsePlaceNames(rawInput);

        if (placeNames.length == 0) {
            return BotResponse.of(update.getMessage().getChatId(), "Місце не може бути порожнім або складатися лише з пробілів");
        }

        if (hasDuplicates(placeNames)) {
            return BotResponse.of(update.getMessage().getChatId(), "У списку місць є дублікати. Будь ласка, видаліть повторювані місця");
        }

        final Round activeRound = roundRepository.findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status.ACTIVE, chatId);
        if (activeRound == null) {
            return BotResponse.of(update.getMessage().getChatId(), "Немає активного раунду");
        }

        final List<Place> existingPlaces = placeRepository.findByRoundId(activeRound.getId());
        final int numberOfNewWords = args.length - 1;
        final int numberOfAvailableWords = MAX_PLACES_PER_ROUND - existingPlaces.size();
        if (existingPlaces.size() + numberOfNewWords > MAX_PLACES_PER_ROUND) {
            return BotResponse.of(update.getMessage().getChatId(), getAvailablePlacesResponse(numberOfAvailableWords));
        }

        return BotResponse.of(update.getMessage().getChatId(), processPlaceNames(update, chatId, placeNames, existingPlaces, activeRound));
    }

    private String processPlaceNames(Update update, Long chatId, String[] placeNames, List<Place> existingPlaces, Round activeRound) {
        return Arrays.stream(placeNames)
                .filter(placeName -> !placeName.isEmpty())
                .map(placeName -> toCamelCase(normalizePlaceName(placeName)))
                .map(placeName -> {
                    if (isNumeric(placeName)) {
                        return "Місце '" + placeName + "' не може складатися лише з чисел\n";
                    }

                    final boolean isDuplicate = existingPlaces.stream()
                            .anyMatch(existingPlace -> levenshteinComparator.compare(
                                    toCamelCase(normalizePlaceName(existingPlace.getName())), placeName));

                    if (isDuplicate) {
                        return "Місце з подібною назвою '" + placeName + "' вже існує в цьому раунді\n";
                    }

                    Place place = new Place();
                    place.setName(placeName);
                    place.setAddedBy(update.getMessage().getFrom().getId());
                    place.setChatId(chatId);
                    place.setRound(activeRound);

                    placeRepository.save(place);
                    return "Місце '" + placeName + "' додано до раунду!\n";
                })
                .collect(Collectors.joining());
    }

    private String getAvailablePlacesResponse(int numberOfAvailablePlaces) {
        if (numberOfAvailablePlaces == 0) {
            return "Досягнута максимальна кількість місць: " + MAX_PLACES_PER_ROUND;
        }

        return "Вільних місць: " + numberOfAvailablePlaces;
    }

    private boolean hasDuplicates(String[] placeNames) {
        final long uniqueCount = Arrays.stream(placeNames)
                .map(this::normalizePlaceName)
                .map(Util::toCamelCase)
                .distinct()
                .count();
        return uniqueCount < placeNames.length;
    }

    private String[] parsePlaceNames(String rawInput) {
        return rawInput.contains(",")
                ? rawInput.split("\\s*,\\s*")
                : new String[]{rawInput};
    }

    private String normalizePlaceName(String placeName) {
        return placeName.trim().replaceAll("\\s+", " ");
    }

    @Override
    public String getCommandName() {
        return Command.ADD_PLACE.getName();
    }
}
