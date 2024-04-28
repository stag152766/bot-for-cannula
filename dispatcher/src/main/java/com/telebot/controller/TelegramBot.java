package com.telebot.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long userId;

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${cannula.delay}")
    private int DELAY;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/set_cannula")) {
                userId = update.getMessage().getFrom().getId();
                setReminder();
                sendTextMessage(chatId, "Таймер запущен. Я напомню вам о необходимости поменять канюлю через 3 дня.");
            }
        }
    }

    private void setReminder() {
        scheduler.schedule(() -> sendReminder(userId), DELAY, TimeUnit.SECONDS);
    }

    private void sendReminder(long userId) {
        sendTextMessage(userId, "Настало время менять канюлю!");
    }

    private void sendTextMessage(long userId, String message) {
        var response = new SendMessage();
        response.setChatId(userId);
        response.setText(message);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }
}
