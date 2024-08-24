package org.archivision.pickbot.bot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Primary
public class BotStarter {
    private final BotRegistrar botRegistrar;
    private final VotingBot votingBot;

    @PostConstruct
    public void start() throws TelegramApiException {
        botRegistrar.register(votingBot);
    }
}
