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

        final String placeName = normalizePlaceName(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

        if (placeName.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Місце не може бути порожнім або складатися лише з пробілів");
        }

        if (isNumeric(placeName)) {
            return BotResponse.of(update.getMessage().getChatId(), "Місце не може складатися лише з чисел");
        }

        final Round activeRound = roundRepository.findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status.ACTIVE, chatId);
        if (activeRound == null) {
            return BotResponse.of(update.getMessage().getChatId(), "Немає активного раунду");
        }

        final List<Place> existingPlaces = placeRepository.findByRoundId(activeRound.getId());
        if (existingPlaces.size() >= MAX_PLACES_PER_ROUND) {
            return BotResponse.of(update.getMessage().getChatId(), "До раунду не можна додати більше ніж " + MAX_PLACES_PER_ROUND + " місця");
        }

        for (Place existingPlace : existingPlaces) {
            String existingPlaceName = normalizePlaceName(existingPlace.getName());
            if (levenshteinComparator.compare(existingPlaceName, placeName)) {
                return BotResponse.of(update.getMessage().getChatId(), "Місце з подібною назвою '" + placeName + "' вже існує в цьому раунді");
            }
        }

        Place place = new Place();
        place.setName(placeName);
        place.setAddedBy(update.getMessage().getFrom().getId());
        place.setChatId(chatId);
        place.setRound(activeRound);

        placeRepository.save(place);

        return BotResponse.of(update.getMessage().getChatId(), "Місце '" + placeName + "' додано до раунду!");
    }

    private String normalizePlaceName(String placeName) {
        return placeName.trim().replaceAll("\\s+", " ");
    }

    @Override
    public String getCommandName() {
        return Command.ADD_PLACE.getName();
    }
}
