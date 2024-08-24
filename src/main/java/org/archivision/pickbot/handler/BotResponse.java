package org.archivision.pickbot.handler;

public record BotResponse(Long chatId, String message) {
    public static BotResponse of(Long chatId, String message) {
        return new BotResponse(chatId, message);
    }
}
