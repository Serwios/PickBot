package org.archivision.pickbot.handler.user;

import jakarta.annotation.PostConstruct;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.Command;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class AdminInfoHandlerUser implements UserCommandHandler {
    private String infoTemplate;

    @PostConstruct
    public void prepareInfoTemplate() {
        this.infoTemplate = """
                Команди Адміна:
                """ +
                Arrays.stream(Command.values())
                        .filter(Command::isAdminCommand)
                        .map(command -> command.getName() + " " + command.getDescription())
                        .collect(Collectors.joining("\n"));
    }


    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        return BotResponse.of(update.getMessage().getChatId(), infoTemplate);
    }

    @Override
    public String getCommandName() {
        return Command.ADMIN.getName();
    }
}
