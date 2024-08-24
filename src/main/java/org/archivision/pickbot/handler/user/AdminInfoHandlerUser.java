package org.archivision.pickbot.handler.user;

import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AdminInfoHandlerUser implements UserCommandHandler {
    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        final String response = """
                Команди адміна:
                /remove round  <індекс_раунду_або_назва> - Видалити раунд
                /remove all rounds - Видалити всі раунди
                /remove place <індекс_раунду_або_назва> <індекс_місця_або_назва> - Видалити місце
                
                /rename round <індекс_раунду_або_назва> <нова_назва> - Перейменувати раунд
                /rename place <індекс_раунду_або_назва> <індекс_місця_або_назва> <нова назва> - Перейменувати місце
                """;

        return BotResponse.of(update.getMessage().getChatId(), response);
    }

    @Override
    public String getCommandName() {
        return Command.ADMIN.getName();
    }
}
