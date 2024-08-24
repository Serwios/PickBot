package org.archivision.pickbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.archivision.pickbot.handler.BotResponse;
import org.archivision.pickbot.handler.CommandHandler;
import org.archivision.pickbot.handler.CommandsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VotingBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final CommandsWrapper commandsWrapper;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            final String[] args = update.getMessage().getText().split(" ");
            final String command = args[0];

            if (isCommand(update)) executeCommandHandler(update, args, getCommandHandler(update, command));
        }
    }

    private boolean isCommand(Update update) {
        return update.getMessage().getText().charAt(0) == '/';
    }

    private CommandHandler getCommandHandler(Update update, String command) {
        return isAdmin(update) ?
                commandsWrapper.getAdminAndUserCommandsStrategy().get(command) :
                commandsWrapper.getUserCommandsStrategy().get(command);
    }

    private boolean isAdmin(Update update) {
        try {
            final GetChatAdministrators getChatAdmins = new GetChatAdministrators();
            getChatAdmins.setChatId(update.getMessage().getChatId());

            final List<ChatMember> administrators = execute(getChatAdmins);
            for (ChatMember member : administrators) {
                if (member.getUser().getId().equals(update.getMessage().getFrom().getId())) {
                    return true;
                }
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void executeCommandHandler(Update update, String[] args, CommandHandler commandHandler) {
        if (commandHandler != null) {
            sendMsg(commandHandler.handle(update, args, update.getMessage().getChatId()));
        } else {
            sendMsg(BotResponse.of(update.getMessage().getChatId(), "Такої команди немає, або вона вам недоступна"));
        }
    }

    private void sendMsg(BotResponse botResponse) {
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
