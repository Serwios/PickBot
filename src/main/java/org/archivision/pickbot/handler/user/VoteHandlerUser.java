package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Round;
import org.archivision.pickbot.entity.Vote;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.repo.VoteRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VoteHandlerUser implements UserCommandHandler {
    private final RoundRepository roundRepository;
    private final PlaceRepository placeRepository;
    private final VoteRepository voteRepository;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        if (args.length < 2) {
            return BotResponse.of(update.getMessage().getChatId(), "Використання: /vote <position_or_name>");
        }

        final String name = args[1];
        final Long userId = update.getMessage().getFrom().getId();

        final Round activeRound = roundRepository.findTopByStatusAndChatIdOrderByStartedAtDesc(Round.Status.ACTIVE, chatId);
        if (activeRound == null) {
            return BotResponse.of(update.getMessage().getChatId(), "Не знайдено активного раунду");
        }

        Place place;
        try {
            int position = Integer.parseInt(name) - 1;

            final List<Place> places = placeRepository.findByRoundOrderByVotesDesc(activeRound);
            if (position < 0 || position >= places.size()) {
                return BotResponse.of(update.getMessage().getChatId(), "Неправильний номер позиції");
            }

            place = places.get(position);
        } catch (NumberFormatException e) {
            place = placeRepository.findByNameAndRound(name, activeRound);
            if (place == null) {
                return BotResponse.of(update.getMessage().getChatId(), "Місце з такою назвою не знайдено");
            }
        }

        if (voteRepository.existsByUserIdAndPlaceAndChatId(userId, place, chatId)) {
            return BotResponse.of(update.getMessage().getChatId(), "Ви вже проголосували за це місце");
        }

        final Vote vote = new Vote();
        vote.setPlace(place);
        vote.setUserId(userId);
        vote.setChatId(chatId);

        voteRepository.save(vote);

        place.setVotes(place.getVotes() + 1);
        placeRepository.save(place);

        return BotResponse.of(update.getMessage().getChatId(), "Ваш голос за '" + place.getName() + "' був успішно записаний");
    }

    @Override
    public String getCommandName() {
        return Command.VOTE.getName();
    }
}
