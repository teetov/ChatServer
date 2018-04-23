package com.teetov.chat.server.connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.teetov.chat.message.StatusList;
import com.teetov.chat.message.Message;
import com.teetov.chat.message.MessageProtocol;
import com.teetov.chat.server.clientbase.ClientsBaseNotFound;
import com.teetov.chat.server.context.AccessType;
import com.teetov.chat.server.context.ServerContext;

public class ConnectionThread implements Runnable {
	
	private ServerContext context;
	
	private Socket socket;
	
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	private boolean alive = true;
	
	private String name;
	
	private int id;

	private Message statusMess;
	
	private AccessChecker checker;
	private final int maxAttempt = 3;
	
	private boolean accessed = false;
	
	private Logger logger = LogManager.getLogger();
	
	ConnectionThread(ServerContext context, Socket socket) {
		this.context = context;
		this.socket = socket;
		
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			logger.fatal("[{}] Object stream was not created", name, e);
		}
		
		checker = context.getAccessChecker();
	}

	/**
	 * Сообщает о текущем состоянии соединения с клиентом.
	 * 
	 * @return {@code true}, если есть возможность принимать и отправлять сообщения 
	 */
	public boolean isAlive() {
		return alive;
	}
	
	/**
	 * Возращает id, под которым этот {@code ConnectionThread} зарегестрирован в {@code ConnectionsManager}.
	 * Вызывать метод имеет смысл только после того как, регистраия будет произведена.
	 * 
	 * @return id соединения или -1, если оно ещё  не было определено
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Отправить сообщение клиенту.
	 * 
	 * @param message сообщение для отправки
	 */
	public synchronized void sendMessage(Message message) {
		if(!alive) 
			return;
		try {
			oos.writeObject(message);
		} catch (IOException e) {
			logger.error("[{}] Can not write Message", name, e);
		}
	}
	
	/**
	 * Прочитать сообщение пришедшее от клинта.
	 * 
	 * @return полученное сообщение
	 */
	public Message readMessage() {
		if(alive) {
			try {
				Message mess = (Message) ois.readObject();
				return mess;
			} catch (Exception e) {
				logger.error("[{}] Can not read Message", name, e);
			}
		}
		return null;
	}
	
	/**
	 * Отправить клиенту инициализаионное сообщение.
	 * В этом сообщении указанна необходимые ему указания для подключению к серверу.
	 * Общение с клинтом следует начинать с этого метода.
	 */
	public void init() {
		String initMess = "";
		if(context.withPassword()) {
			initMess += MessageProtocol.REQUIRED_PASSWORD;
		}
		sendMessage(context.getMessage(initMess, MessageProtocol.INITIALIZE));
		
	}
	
	/**
	 * Обработка пришедшего от клиента инииализаионного сообщения.
	 * 
	 * @param message сообщение, где {@code Message.getDestination() == MessageProtocol.INITIALIZE}
	 */
	private void initMessage(Message message) {
		name = message.getName();
	}
	
	/**
	 * Проверяет может ли клиент получить доступ к чату. 
	 * Если доступ получен производит добавляет данное подключение в {@code ConnectionsManager}.
	 * 
	 * @param message сообщение, где {@code Message.getDestination() == MessageProtocol.ACCESS}
	 * @return true если додключение прошло проверки и было успешно добавлено 
	 */			
	private void access(Message message) {
		
		if(accessed) {
			return;
		}
		
		if(context.getAccessOptions().equals(AccessType.LOGIN) ) {
			String password = message.getBody();
			
			try {
				checker.correctLogin(name, password);
			} catch (ClientsBaseNotFound e) {
				logger.error("[{}] Client base missing", name, e);
			}
			
		} else if (context.getAccessOptions().equals(AccessType.PASSWORD)) {
			String password = message.getBody();
			
			checker.correctPassword(password);
		}
		
		synchronized(context.getConnectionsManager()) {			
			checker.availableConnection();
			if(checker.check()) {
				id = context.getConnectionsManager().add(this);
				accessed = true;
			}
		}
		
		logger.info(checker.getStatus());

		checker.increaseAttemptCount();
		
		if(checker.check()) {
			successAccess();
		} else {
			failedAccess();
		}
	}

	/**
	 * Выполянется если клиент получил полноценный доступ к серверу.
	 */
	private void successAccess() {
		Message response = context.getMessage(MessageProtocol.ACCESSED, MessageProtocol.LOGIN);
		sendMessage(response);

		sendMessage(context.getMessage(checker.getStatus()));
		
		context.getConnectionsManager().sendLastMessages(this);
		context.getConnectionsManager().sayHelloToOthers(this);
	}
	
	/**
	 * Выполянется если клиент не получил полноценного доступа к серверу.
	 */
	private void failedAccess() {
		Message response = context.getMessage(MessageProtocol.DENIED, MessageProtocol.LOGIN);	
		sendMessage(response);
		
		sendMessage(context.getMessage(checker.getStatus()));

		if(!isFatalProblem() && checker.getAttemptCount() < maxAttempt) {
			sendMessage(context.getMessage("Осталось попыток: " + 
					(maxAttempt - checker.getAttemptCount())));
			init();
			logger.info("[{}] Connection attempt №{}", name, checker.getAttemptCount());
		} else {
			terminate();
		}
	}
	
	/**
	 * Определяет можно ли дать клиенту возможность решить проблему,
	 *  возникшую при проверки возможности доступа.
	 *  
	 * @return {@code true}, если у клиента есть возможность испривить проблемы, не разрывая соединения
	 */
	private boolean isFatalProblem() {
		if(checker.hasProblem(AccessChecker.Problem.CONNECTION) ||
				checker.hasProblem(AccessChecker.Problem.NAME) ||
				checker.hasProblem(AccessChecker.Problem.NAME_TAKEN)) {
			return true;
		}
		return false;
	}	
	
	
	/**
	 * Обработка пришедшего от клиента текстового сообщения.
	 * 
	 * @param message сообщение, где {@code Message.getDestination() == MessageProtocol.TEXT}
	 */
	private void textMessage(Message message) {
		if(accessed) {
			context.getConnectionsManager().receiveMessage(message);
		} else {
			logger.warn("[{}] Receive message from uninitialized client", name);
		}
	}
	
	/**
	 * Обработка пришедшего от клиента сообщения о смене текущего статуса.
	 * 
	 * @param message сообщение, где {@code Message.getDestination() == MessageProtocol.STATUS}
	 */
	private void status(Message message) {
		statusMess = statusTransform(message);
		logger.info("Status was saved {} dest {}", statusMess, statusMess.getDestination());
		context.getConnectionsManager().sedMessageToAllExcept(statusMess, this);
	}
	
	/**
	 * Преобразует статусное сообщения, добавляя в тело id клиента.
	 * @param message сообщение, пришедшее от пользователя
	 * @return сообщение, готовое к отправки другим клиентам
	 */
	private Message statusTransform(Message message) {
		String body = id + "#" + message.getBody();
		message.setBody(body);
		return message;
	}
	
	/**
	 * Последнне статусное сообщение пришедшее от пользователя.
	 * @return {@code null} еслипользователь ещё не выбирал статус
	 */
	public Message getStatus() {
		return statusMess;
	}
	
	/**
	 * Формирует сообщение, которое проинформирует других пользователей, о выходе данного пользователя из чата.
	 * @return готовое для отправки сообщение
	 */
	public Message getExitStatusMessage() {
		return new Message(String.valueOf(id) + "#" + StatusList.getExitStustus(), name, socket.getInetAddress().toString(), MessageProtocol.STATUS);
	}
	
	/**
	 * Формирует сообщение, которое проинформирует других пользователей, о входе данного пользователя в чат.
	 * @return готовое для отправки сообщение
	 */
	public Message getWelcomStatusMessage() {
		if(statusMess != null) 
			return statusMess;
		return new Message(String.valueOf(id), name, socket.getInetAddress().toString(), MessageProtocol.STATUS);
	}
	
	/**
	 * * Обработка пришедшего от клиента тестового сообщения.
	 * Прекращает приём других сoобщений.
	 * @param message message сообщение, где {@code Message.getDestination() == MessageProtocol.TEST}
	 */
	private void testMessage(Message message) {
		logger.info("[{}] Someone tring to get information about this server", name);
		
		alive = false;
	}
	
	/**
	 * Определяет как именно следует обрабатывать полученное от клиента сообщение.
	 * 
	 * @param message новое сообщение
	 */
	private void processMessage(Message message) {
		int dest = message.getDestination();
		
		switch (dest) {
		case MessageProtocol.INITIALIZE:
			initMessage(message);
			break;
		case MessageProtocol.LOGIN:
			access(message);
			break;
		case MessageProtocol.TEXT:
			textMessage(message);
			break;
		case MessageProtocol.STATUS:
			status(message);
			break;
		case MessageProtocol.TEST:
			testMessage(message);
			break;
		case MessageProtocol.TERMINATE:
			terminate();
			break;
		default:
			logger.warn("[{}] Unprocessed message: \"{}\" with unknown destination status: {}",
					name, message, message.getDestination());
		}
	}
	
	@Override
	public void run() {
		init();		

		try {
			while (alive) {
				Message mess = (Message) ois.readObject();
				logger.info(mess.toString() + " " + mess.getDestination());

				processMessage(mess);
			}
		} 
		catch(SocketException e) {
			logger.warn("[{}] Connection terminated on client side", name);
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			if(accessed) {
				context.getConnectionsManager().sayGoodbyeToOthers(this);
				context.getConnectionsManager().remove(this);
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Возвращает имя пользователя, общающегося через это соединение.
	 * @return имя пользователя
	 */
	public String getName() {
		return name;	
	}
	
	/**
	 * Останавливает процесс взаимодействия с клиентом. 
	 */
	private void terminate() {
		sendMessage(context.getMessage("bye", MessageProtocol.TERMINATE));
		logger.info("[{}] Connection terminated", name);
		
		alive = false;
		
	}
}
