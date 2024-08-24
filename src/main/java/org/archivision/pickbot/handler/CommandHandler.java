package org.archivision.pickbot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    BotResponse handle(Update update, String[] args, Long chatId);
    String getCommandName();
}
