package org.archivision.pickbot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

@FunctionalInterface
public interface SubCommandProcessor {
    BotResponse process(Update update, String[] args, Long chatId);
}
