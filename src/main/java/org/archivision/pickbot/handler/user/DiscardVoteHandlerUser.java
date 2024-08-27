package org.archivision.pickbot.handler.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.entity.Place;
import org.archivision.pickbot.entity.Vote;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.PlaceRepository;
import org.archivision.pickbot.repo.VoteRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscardVoteHandlerUser implements UserCommandHandler {
    private final PlaceRepository placeRepository;
    private final VoteRepository voteRepository;

    @Override
    @Transactional
    public BotResponse handle(Update update, String[] args, Long chatId) {
        final Long userId = update.getMessage().getFrom().getId();

        final List<Vote> votes = voteRepository.findByUserIdAndChatId(userId, chatId);
        if (votes.isEmpty()) {
            return BotResponse.of(update.getMessage().getChatId(), "Ви ще не проголосували в цьому чаті");
        }

        // presumably unique one vote per (place-round)
        final Vote vote = votes.get(0);
        final Place place = vote.getPlace();

        voteRepository.delete(vote);

        place.setVotes(place.getVotes() - 1);
        placeRepository.save(place);

        return BotResponse.of(update.getMessage().getChatId(), "Ваш голос за '" + place.getName() + "' був успішно видалений");
    }

    @Override
    public String getCommandName() {
        return Command.DISCARD.getName();
    }
}
