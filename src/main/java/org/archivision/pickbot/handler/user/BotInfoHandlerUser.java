package org.archivision.pickbot.handler.user;

import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class BotInfoHandlerUser implements UserCommandHandler {
    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        final String response = """
                Цей бот допомагає проводити пріоритетне голосування за події або місця, а також відслідковувати історію голосувань

                Команди користувача:
                /start <назва_раунду> - Розпочати новий раунд голосування
                /add <назва_місця> - Додати місце до активного раунду
                /list - Показати список місць у поточному раунді
                /vote <назва_місця_або_індекс> - Проголосувати за місце
                /end - Завершити раунд голосування
                /rounds - Показати список усіх раундів
                /round <індекс_раунду> - Показати інформацію про конкретний раунд
                /admin - Команди адміна
                """;

        return BotResponse.of(update.getMessage().getChatId(), response);
    }

    @Override
    public String getCommandName() {
        return Command.INFO.getName();
    }
}
