package com.teetov.chat.server.connections;

import java.util.EnumSet;
import java.util.Set;

import com.teetov.chat.server.clientbase.ClientbaseException;
import com.teetov.chat.server.clientbase.ClientValidator;
import com.teetov.chat.server.clientbase.ClientValidatorFactory;
import com.teetov.chat.server.context.ServerContext;

/**
 *Класс содердит набор проверок, которые должен пройти клиент, прежде чем подключится к серверу.
 * 
 *
 */
public class AccessChecker {
    
    private ServerContext context;
    private ClientValidatorFactory clientsProducer;
    
    private int attemptCount = 0;
    
    private final static String SUCCESS = "Подключение доступно";
    private final static String WRONG_PASSWORD = "Неверный пароль";
    private final static String WRONG_NAME = "Нет пользователя с таким именем";
    private final static String ABSENT_CONNECTION = "Нет доступных подключений";
    private final static String NAME_ALREADY_TAKEN = "Имя уже занято";
    
    public static enum Problem {
        CONNECTION(ABSENT_CONNECTION), PASSWORD(WRONG_PASSWORD), NAME(WRONG_NAME), NAME_TAKEN(NAME_ALREADY_TAKEN);
        
        Problem(String i) {
            info = i;
        }
        
        private String info;
        
        public String toString() {
            return info;
        }
    }
    
    private Set<Problem> problems = EnumSet.noneOf(Problem.class);
    
    public AccessChecker(ServerContext context, ClientValidatorFactory clientsProducer) {
        this.context = context;
        this.clientsProducer = clientsProducer;
    }
    
    /**
     * Проверка наличия доступного подключения в {@code ConnectionsManager}.
     */
    public boolean availableConnection() {
        boolean result = true;
        if(!context.getConnectionsManager().hasAvaliebelConnection()) {
            problems.add(Problem.CONNECTION);
            result = false;
        } else {
            problems.remove(Problem.CONNECTION);
        }
        return result;
    }
    
    /**
     * Проверка корректности пароля.
     * Следует вызывать, если {@code ServerContext.getAccessOption() == AccessOption.PASSWORD}.
     * @param password - серверный пароль
     * @return {@code true} если проверка успешно пройдена
     */
    public boolean correctPassword(String password) {
        boolean result = password.equals(context.getPassword());
        
        if(!result) {
            problems.add(Problem.PASSWORD);
        }
        
        return result;
    }
    
    /**
     * Проверка корректности логина и пароля.
     * Следует вызывать, если {@code ServerContext.getAccessOption() == AccessOption.LOGIN}.
     * 
     * @param login - имя клиента
     * @param password - пароль клиента
     * @return {@code true} если проверка успешно пройдена
     * 
     * @throws ClientbaseException
     */
    public boolean correctLogin(String login, String password) throws ClientbaseException{
        boolean result = true;
        
        ClientValidator clients = clientsProducer.getClientValidator();
        
        if(context.getConnectionsManager().getActiveNameSet().contains(login)) {
            problems.add(Problem.NAME_TAKEN);
            result = false;
        }
        
        if(!clients.isCorrectName(login)) {
            problems.add(Problem.NAME);
            result = false;        
        } 
        else if(!clients.isCorrectPassword(login, password)) {
            problems.add(Problem.PASSWORD);
            result = false;
        } else {
            problems.remove(Problem.PASSWORD);
        }
        return result;
    }
    
    /**
     * Собщает следует ли предоставить возможность просителю подключиться к чату.
     * Данный метод следует вызывать после проведение интересующих проверок
     * (например {@code availableConnection()}).
     * 
     * @return результат проведённых проверок
     */
    public boolean check() {
        return problems.size() == 0;
    }
    
    public void increaseAttemptCount() {
        attemptCount++;
    }
    
    public int getAttemptCount() {
        return attemptCount;
    }
    
    /**
     * Возвращает собщение о статусе проверки возможности доступа.
     * Сообщение содержит либо упоминание об успехе, либо перечень проблем.
     * 
     * @return статус проверки в текстовом виде.
     */    
    public String getStatus() {
        if(check()) {
            return SUCCESS;
        }
        StringBuilder result = new StringBuilder("Не удалось установить подлючение: ");
        for(Problem prob : problems) {
            result.append(prob.toString());
            result.append(", ");
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }    

    public boolean hasProblem(AccessChecker.Problem problem) {
        return problems.contains(problem);
    }
}
