package org.archivision.pickbot.handler;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.archivision.pickbot.handler.admin.AdminCommandHandler;
import org.archivision.pickbot.handler.developer.DeveloperCommandHandler;
import org.archivision.pickbot.handler.user.UserCommandHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandsWrapper {
    private final List<UserCommandHandler> userCommandHandlers;
    private final List<AdminCommandHandler> adminCommandHandlers;
    private final List<DeveloperCommandHandler> developerCommandHandlers;

    @Getter
    private Map<String, UserCommandHandler> userCommandsStrategy;

    @Getter
    private Map<String, CommandHandler> adminAndUserCommandsStrategy;

    @Getter
    private Map<String, CommandHandler> developerCommandStrategy;

    @PostConstruct
    public void init() {
        userCommandsStrategy = new HashMap<>();
        adminAndUserCommandsStrategy = new HashMap<>();
        developerCommandStrategy = new HashMap<>();

        for (UserCommandHandler userCommandHandler : userCommandHandlers) {
            userCommandsStrategy.put(userCommandHandler.getCommandName(), userCommandHandler);
            adminAndUserCommandsStrategy.put(userCommandHandler.getCommandName(), userCommandHandler);
            developerCommandStrategy.put(userCommandHandler.getCommandName(), userCommandHandler);
        }

        for (AdminCommandHandler adminCommandHandler : adminCommandHandlers) {
            adminAndUserCommandsStrategy.put(adminCommandHandler.getCommandName(), adminCommandHandler);
            developerCommandStrategy.put(adminCommandHandler.getCommandName(), adminCommandHandler);
        }

        for (DeveloperCommandHandler developerCommandHandler : developerCommandHandlers) {
            developerCommandStrategy.put(developerCommandHandler.getCommandName(), developerCommandHandler);
        }
    }
}
