package org.archivision.pickbot.util;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TemplateGenerator {
    public String parsePlacesToResponseTemplate(List<Place> places) {
        final StringBuilder response = new StringBuilder("Місця в раунді:\n");

        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            int positionIndex = i + 1;
            response.append(positionIndex).append(". ")
                    .append(place.getName()).append(" - ")
                    .append(place.getVotes()).append(" голоси\n");
        }

        return response.toString();
    }

    public String parseRoundsToResponseTemplate(List<Round> roundOrderedDesc) {
        final StringBuilder response = new StringBuilder("Раунди:\n");

        for (int i = 0; i < roundOrderedDesc.size(); i++) {
            Round round = roundOrderedDesc.get(i);
            int ordinalIndex = i + 1;
            response.append(ordinalIndex).append(". ")
                    .append(round.getName()).append(" - ")
                    .append(round.getStatus()).append("\n");
        }

        return response.toString();
    }
}
