package org.archivision.pickbot.handler.user;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.archivision.pickbot.repo.RoundRepository;
import org.archivision.pickbot.util.TemplateGenerator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ListRoundsHandlerUser implements UserCommandHandler {
    private final RoundRepository roundRepository;
    private final TemplateGenerator templateGenerator;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        return BotResponse.of(
                update.getMessage().getChatId(),
                templateGenerator.parseRoundsToResponseTemplate(roundRepository.findByChatId(chatId))
        );
    }

    @Override
    public String getCommandName() {
        return Command.ROUNDS.getName();
    }
}
