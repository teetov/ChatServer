package com.teetov.chat.server.connections;

import java.util.*;
import java.util.stream.Collectors;

import com.teetov.chat.message.Message;

public class ConnectionsManager {
	private volatile Set<ConnectionThread> connections = new HashSet<>();
	
	private volatile LastMessageHolder messages = new LastMessageHolder();	
	
	private final Integer maxConnect;
	
	private volatile SortedSet<Integer> idSet = new TreeSet<>();
	
	public ConnectionsManager(int maxConnection) {
		maxConnect = maxConnection;
	}
	
	/**
	 * Добавляет (регистрирует) новое соедининение. 
	 * Связанный с ним клиент будет получать новые сообщения, от других клиентов.
	 * 
	 * @param connection соединение, которое нужно добавить
	 */
	public synchronized int add(ConnectionThread connection) {
		connections.add(connection);
		
		int id = nextId();
		idSet.add(id);
		return id;
	}	
	
	/**
	 * Возвращает новый id для соединения, не использующейся в текущем наборе.
	 * @return уникальный id
	 */
	private int nextId() {
		if(idSet.size() == 0)
			return 1;
		int id = idSet.last() + 1;
		if(id >= maxConnect * 100) {
			id = 1;
			while(idSet.contains(id)) {
				id++;
			}
		}
		return id;
	}
	
	/**
	 * Отправить клиенту последнние пришедшие на сервер сообщения.
	 * @param connection соединения с клиентом, которомубудет отправлены сообщения
	 */
	public void sendLastMessages(ConnectionThread connection) {
		for(Message mess : messages.getTenMessage())
			connection.sendMessage(mess);
	}
	
	/**
	 * Отправить другим клиентам сообщение о том, что данный клиент подключился к общему чату.
	 * После чего подключившемуся клиенту будет отправлены ответные сообщения от их имени. 
	 * @param connection соединение с клиентом, от имение которого необходимо разослать приветственные сообщения
	 */
	public void sayHelloToOthers(ConnectionThread connection) {
		sedMessageToAllExcept(connection.getWelcomStatusMessage(), connection);
		
		List<Message> hellowMessFromOthers = new ArrayList<>();
		synchronized(this) {
			for(ConnectionThread conn : connections) {
				if(conn != connection) 
					hellowMessFromOthers.add(conn.getWelcomStatusMessage());
			}
		}
		
		for(Message mess : hellowMessFromOthers) 
			connection.sendMessage(mess);
	}
	
	/**
	 * Отправить другим клиентам сообщение о том, что данный клиент подключился к общему чату.
	 * @param connection соединение с клиентом, от имение которого необходимо разослать приветственные сообщения
	 */
	public void sayGoodbyeToOthers(ConnectionThread connection) {
		sedMessageToAllExcept(connection.getExitStatusMessage(), connection);
	}
	
	/**
	 * Обрабатывает новое сообщение. оно будет отправлено всем зарегестрированным клиетнам.
	 * А так же добавлено в список последних сообщений.
	 * 
	 * @param message сообщение которое нужно обработать
	 */
	public void receiveMessage(Message message) {	
		sedMessageToAll(message);			
		messages.add(message);
	} 
	
	/**
	 * Мтод отправяет сообщение всем зарегистрированным клиентам.
	 * 
	 * @param message сообщение для отправки
	 */
	private synchronized void sedMessageToAll(Message message) {	
		for(ConnectionThread con : connections) {
			con.sendMessage(message);
		}
	}
	
	/**
	 * Отправляет сообщение всем клиентам за исключением одного.
	 * @param message сообщение для отправки
	 * @param connection соединение с клиентом, которое следует обойти
	 */
	public synchronized void sedMessageToAllExcept(Message message, ConnectionThread connection) {

		for(ConnectionThread con : connections) {
			if(con != connection)
				con.sendMessage(message);
		}
	}

	
	/**
	 * Удаляет данное соединение из общего списка соединений. Тем самым освобожная место для нового.
	 * Связанный с ним клиент перестанет получать сообщения.
	 * 
	 * @param connection - поток соединени¤ который необходимо удалить
	 */
	public synchronized void remove(ConnectionThread connection) {
		connections.remove(connection);
		idSet.remove(connection.getId());
	}
	
	/**
	 * Метод сообщает есть ли доступное место для нового соединения
	 * 
	 * @return {@code true} если список соединений ещё не заполнен
	 */
	public synchronized boolean hasAvaliebelConnection() {
		return connections.size() < maxConnect;
	}
	
	/**
	 * Возвращает множество имён подключённых пользавателей.
	 * @return имена пользователей
	 */
	public Set<String> getActiveNameSet() {
		Set<ConnectionThread> connectTemp = null;
		synchronized(this) {
		connectTemp = new HashSet<>(connections);
		}
		
		Set<String> result = new HashSet<>();
		for(ConnectionThread conn : connectTemp) {
			result.add(conn.getName());
		}
		
		return result;
	}
}
