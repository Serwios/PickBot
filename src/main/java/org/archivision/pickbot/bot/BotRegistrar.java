package org.archivision.pickbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
public class BotRegistrar {
    public void register(LongPollingBot telegramBot) throws TelegramApiException {
        try {
            log.info("Registering bot... Username: {}, Token: {}",
                    telegramBot.getBotUsername(),
                    telegramBot.getBotToken()
            );

            new TelegramBotsApi(DefaultBotSession.class).registerBot(telegramBot);
            log.info("Telegram bot is ready to accept updates from user");
        } catch (TelegramApiRequestException e) {
            log.error("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running)", e);
        }
    }
}
