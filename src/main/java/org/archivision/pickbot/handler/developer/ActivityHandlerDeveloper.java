package org.archivision.pickbot.handler.developer;

import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.service.UserStatService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.archivision.pickbot.handler.Command.ACTIVITY;

@Component
@RequiredArgsConstructor
public class ActivityHandlerDeveloper implements DeveloperCommandHandler {
    private final UserStatService userStatService;

    @Override
    public BotResponse handle(Update update, String[] args, Long chatId) {
        int days = 0;
        if (args.length > 0) {
            try {
                days = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return BotResponse.of(chatId, "Некоректна форма для кількості днів");
            }
        }

        return BotResponse.of(chatId, String.valueOf(userStatService.countActiveUsers(days)));
    }

    @Override
    public String getCommandName() {
        return ACTIVITY.getName();
    }
}
