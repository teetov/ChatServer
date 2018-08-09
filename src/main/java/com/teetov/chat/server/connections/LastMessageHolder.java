package com.teetov.chat.server.connections;

import java.util.*;

import com.teetov.chat.message.Message;

/**
 *  Класс хранит последние полученные сервером сообщения.
 * 
 * @author  Aleksey Titov
 *
 */
public class LastMessageHolder {

    private final int MAX_MESSAGES = 10;
    
    private List<Message> holder = new LinkedList<>();
    
    /**
     * Добавляет новое сообщение.
     * 
     * @param message сообщение, которое необходимо добавить
     */
    public synchronized void add(Message message) {
        if(holder.size() >= MAX_MESSAGES) {
            holder.remove(0);
        }
        holder.add(message);
    }
    
    /**
     * Возвращает список последних сообщений.
     * 
     * @return список в порядке добавления
     */
    public synchronized List<Message> getTenMessage() {
        return new ArrayList<>(holder);
    }
    
}
