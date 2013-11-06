package chessCore;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

public class GameStateExchanger {
	private Connection connection;
	private String gameUUID;
	private Session session;
	private Queue gameQueue;
	private Destination respondQueue;
	
	public GameStateExchanger(Connection connection, String gameUUID) throws JMSException {
		this.connection = connection;
		this.gameUUID = gameUUID;
		this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.gameQueue = this.session.createQueue("g" + this.gameUUID.replace('-', '_'));
	}
	
	public GameState PushAndGetGameState(GameState gameState) throws JMSException {
		MessageProducer messageProducer = this.session.createProducer(this.gameQueue);
		ObjectMessage objectMessage = this.session.createObjectMessage(gameState);
		TemporaryQueue responseQueue = this.session.createTemporaryQueue();
		objectMessage.setJMSReplyTo(responseQueue);
		messageProducer.send(objectMessage);
		messageProducer.close();
		
		MessageConsumer messageConsumer = this.session.createConsumer(responseQueue);
		ObjectMessage responseMessage = (ObjectMessage)messageConsumer.receive();
		messageConsumer.close();
		return (GameState)responseMessage.getObject();
	}
	
	public GameState GetGameState() throws JMSException {
		MessageConsumer messageConsumer = this.session.createConsumer(this.gameQueue);
		ObjectMessage responseMessage = (ObjectMessage)messageConsumer.receive();
		this.respondQueue = responseMessage.getJMSReplyTo();
		messageConsumer.close();
		return (GameState)responseMessage.getObject();
	}
	
	public void RespondWithGameState(GameState gameState) throws JMSException {
		MessageProducer messageProducer = this.session.createProducer(this.respondQueue);
		ObjectMessage objectMessage = this.session.createObjectMessage(gameState);
		messageProducer.send(objectMessage);
		messageProducer.close();
	}
}
