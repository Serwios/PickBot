package org.archivision.pickbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.CommandHandler;
import org.archivision.pickbot.handler.CommandsWrapper;
import org.archivision.pickbot.service.UserStatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class VotingBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${developer.name}")
    private String developerName;

    private final CommandsWrapper commandsWrapper;
    private final UserStatService userStatService;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (isUpdateValid(update) && isCommand(update)) {
            final String[] args = update.getMessage().getText().split(" ");
            final String command = args[0];

            final User user = update.getMessage().getFrom();
            userStatService.recordInteraction(user.getId(), user.getUserName());

            executeCommandHandler(update, args, getCommandHandler(update, command));
        }
    }

    private boolean isUpdateValid(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    private boolean isCommand(Update update) {
        return update.getMessage().getText().charAt(0) == '/';
    }

    private CommandHandler getCommandHandler(Update update, String command) {
        if (containsBotNamePostfix(command)) {
            command = removeBotNamePostfix(command);
        }

        if (isFromDeveloper(update)) {
            return commandsWrapper.getDeveloperCommandStrategy().get(command);
        }

        return isFromAdmin(update) ?
                commandsWrapper.getAdminAndUserCommandsStrategy().get(command) :
                commandsWrapper.getUserCommandsStrategy().get(command);
    }

    private boolean containsBotNamePostfix(String command) {
        return command.contains("@" + botUsername);
    }

    private String removeBotNamePostfix(String command) {
        return command.substring(0, command.indexOf('@'));
    }

    private boolean isFromDeveloper(Update update) {
        return update.getMessage().getFrom().getUserName().equals(developerName);
    }

    private boolean isFromAdmin(Update update) {
        try {
            final GetChatAdministrators getChatAdmins = new GetChatAdministrators();
            getChatAdmins.setChatId(update.getMessage().getChatId());

            final ArrayList<ChatMember> adminChatMembers = execute(getChatAdmins);
            for (ChatMember admin : adminChatMembers) {
                if (isUpdateFromChatMember(update, admin)) {
                    return true;
                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isUpdateFromChatMember(Update update, ChatMember member) {
        return member.getUser().getId().equals(update.getMessage().getFrom().getId());
    }

    private void executeCommandHandler(Update update, String[] args, CommandHandler commandHandler) {
        if (commandHandler != null) {
            sendMsg(commandHandler.handle(update, args, update.getMessage().getChatId()));
        } else {
            sendMsg(BotResponse.of(update.getMessage().getChatId(), "Такої команди немає, або вона вам недоступна"));
        }
    }

    public void sendMsg(BotResponse botResponse) {
        final SendMessage message = new SendMessage();
        message.setChatId(botResponse.chatId());
        message.setText(botResponse.message());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
