package org.archivision.pickbot.service;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.repo.PlaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.archivision.pickbot.service.Util.isWholeStringNumeric;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    public Optional<Place> findPlaceByIdentifier(Long roundId, String identifier) {
        if (isWholeStringNumeric(identifier)) {
            int ordinalIndex = Integer.parseInt(identifier) - 1;
            List<Place> places = placeRepository.findByRoundId(roundId);
            if (ordinalIndex >= 0 && ordinalIndex < places.size()) {
                return Optional.of(places.get(ordinalIndex));
            }
        }
        return placeRepository.findByRoundIdAndName(roundId, identifier);
    }

    public Optional<Place> findPlaceByIndex(Long roundId, int index) {
        final List<Place> places = placeRepository.findByRoundId(roundId);
        return (index >= 0 && index < places.size()) ? Optional.of(places.get(index)) : Optional.empty();
    }
}
