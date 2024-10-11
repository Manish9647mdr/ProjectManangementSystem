package com.tkk.service;

import com.tkk.modal.Chat;
import com.tkk.repository.ChatRepository;

public class ChatServiceImpl implements ChatService {
    private ChatRepository chatRepository;

    @Override
    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }
}
