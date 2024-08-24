package org.archivision.pickbot.handler;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.handler.admin.AdminCommandHandler;
import org.archivision.pickbot.handler.user.UserCommandHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandsWrapper {
    private final List<UserCommandHandler> userCommandHandler;
    private final List<AdminCommandHandler> adminCommandHandler;

    @Getter
    private Map<String, UserCommandHandler> userCommandsStrategy;

    @Getter
    private Map<String, CommandHandler> adminAndUserCommandsStrategy;

    @PostConstruct
    public void init() {
        userCommandsStrategy = new HashMap<>();
        adminAndUserCommandsStrategy = new HashMap<>();

        for (UserCommandHandler userCommandHandler : userCommandHandler) {
            userCommandsStrategy.put(userCommandHandler.getCommandName(), userCommandHandler);
            adminAndUserCommandsStrategy.put(userCommandHandler.getCommandName(), userCommandHandler);
        }

        for (AdminCommandHandler adminCommandHandler : adminCommandHandler) {
            adminAndUserCommandsStrategy.put(adminCommandHandler.getCommandName(), adminCommandHandler);
        }
    }
}
