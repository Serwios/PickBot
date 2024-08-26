package org.archivision.pickbot.service;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TemplateGenerator {
    public String parsePlacesToResponseTemplate(List<Place> places) {
        final StringBuilder response = new StringBuilder("Місця в раунді:\n");

        for (int i = 0; i < places.size(); i++) {
            final Place place = places.get(i);
            final int positionIndex = i + 1;
            response.append(positionIndex).append(". ")
                    .append(place.getName()).append(" - ")
                    .append(place.getVotes()).append(" голоси\n");
        }

        return response.toString();
    }

    public String parseRoundsToResponseTemplate(List<Round> roundOrderedDesc) {
        final StringBuilder response = new StringBuilder("Раунди:\n");

        for (int i = 0; i < roundOrderedDesc.size(); i++) {
            final Round round = roundOrderedDesc.get(i);
            final int ordinalIndex = i + 1;
            response.append(ordinalIndex).append(". ")
                    .append(round.getName()).append(" - ")
                    .append(getFormatedTime(round.getStartedAt())).append(" - ")
                    .append(round.getStatus()).append("\n");
        }

        return response.toString();
    }

    private String getFormatedTime(LocalDateTime localDateTime) {
        return localDateTime.getDayOfMonth() + "." +
                localDateTime.getMonthValue()  + "." +
                localDateTime.getYear() + "-" +
                localDateTime.getHour() + ":" +
                localDateTime.getMinute();
    }
}
