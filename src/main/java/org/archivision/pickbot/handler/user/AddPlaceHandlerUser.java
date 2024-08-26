package org.archivision.pickbot.handler.user;

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

import static org.apache.commons.lang3.StringUtils.isNumeric;

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

        StringBuilder responseMessage = new StringBuilder();
        for (String placeName : placeNames) {
            if (placeName.isEmpty()) continue;

            placeName = toCamelCase(normalizePlaceName(placeName));

            if (isNumeric(placeName)) {
                responseMessage.append("Місце '").append(placeName).append("' не може складатися лише з чисел.\n");
                continue;
            }

            String finalPlaceName = placeName;
            boolean isDuplicate = existingPlaces.stream()
                    .anyMatch(existingPlace -> levenshteinComparator.compare(
                            toCamelCase(normalizePlaceName(existingPlace.getName())), finalPlaceName));

            if (isDuplicate) {
                responseMessage.append("Місце з подібною назвою '").append(placeName).append("' вже існує в цьому раунді.\n");
                continue;
            }

            Place place = new Place();
            place.setName(placeName);
            place.setAddedBy(update.getMessage().getFrom().getId());
            place.setChatId(chatId);
            place.setRound(activeRound);

            placeRepository.save(place);
            responseMessage.append("Місце '").append(placeName).append("' додано до раунду!\n");
        }

        return BotResponse.of(update.getMessage().getChatId(), responseMessage.toString().trim());
    }

    private String getAvailablePlacesResponse(int numberOfAvailablePlaces) {
        if (numberOfAvailablePlaces == 0) {
            return "Досягнута максимальна кількість місць: " + MAX_PLACES_PER_ROUND;
        }

        return "ВІльних місць: " + numberOfAvailablePlaces;
    }

    private boolean hasDuplicates(String[] placeNames) {
        long uniqueCount = Arrays.stream(placeNames)
                .map(this::normalizePlaceName)
                .map(this::toCamelCase)
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

    private String toCamelCase(String input) {
        String[] words = input.split("\\s+");
        StringBuilder camelCaseString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                camelCaseString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
        }

        return camelCaseString.toString();
    }

    @Override
    public String getCommandName() {
        return Command.ADD_PLACE.getName();
    }
}
