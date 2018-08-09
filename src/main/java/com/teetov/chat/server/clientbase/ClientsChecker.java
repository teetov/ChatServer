package com.teetov.chat.server.clientbase;

/**
 * Предоставляет возможность обратиться к базе клиентов, 
 * чтобы проверить корректность имеи пользователя и пароля.
 * 
 * @author  Aleksey Titov
 *
 */
public interface ClientsChecker {
    
    /**
     * Проверяет соответствует ли пароль данному имени.
     * 
     * @param name имя для проверки
     * @param password 
     * @return {@code true} если указана коррестная пара именя-пароль.
     */
    boolean isCorrectPassword(String name, String password);
    
    /**
     * Проверяет присутствует ли имя в базе имён.
     * 
     * @param name имя для проверки
     * @return {@code true} если имя найдено
     */
    boolean isCorrectName(String name);
}

