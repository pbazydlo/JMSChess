package chessCore;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;


public class PartnerFinder {
	private Object passiveWaitLock = new Object();
	
	private Connection connection;
	private Session session;
	private Queue announceQueue;
	private TemporaryQueue announceResponseQueue;
	private Queue searchQueue;
	private MessageConsumer searchMessageConsumer;
	private MessageProducer announceMessageProducer;
	private MessageConsumer announceResponseMessageConsumer;
	
	public String GameUUID;
	/*private Responder announceResponder;
	private Responder searchResponder;*/
	private boolean isMatched;
	public void SetIsMatchedAndNotify() {
		synchronized (this.passiveWaitLock) {
			this.isMatched = true;
			this.passiveWaitLock.notify();
		}
	}
	
	public PartnerFinder(Connection connection) throws JMSException {
		this.connection=connection;
	}
	
	public void Find(Color preferredColor) throws JMSException {
		this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		String announceQueueName = "BLACK";
		if(preferredColor==Color.Black) {
			this.AnnounceAndListenForResponses(announceQueueName);
		}
		else {
			this.GetExistingPartner(announceQueueName);
			this.isMatched=true;
		}
		
		this.session.close();
	}
	
	private void GetExistingPartner(String announceQueueName) throws JMSException {
		this.searchQueue = this.session.createQueue(announceQueueName);
		this.searchMessageConsumer = this.session.createConsumer(this.searchQueue);
		Message message = this.searchMessageConsumer.receive();
		MessageProducer messageProducer = this.session.createProducer(message.getJMSReplyTo());
		TextMessage response = this.session.createTextMessage();
		this.GameUUID = java.util.UUID.randomUUID().toString();
		System.out.println("Found partner sending game UUID: " + this.GameUUID);
		response.setText(this.GameUUID);
		messageProducer.send(response);
		messageProducer.close();
		this.searchMessageConsumer.close();
	}
	
	private void AnnounceAndListenForResponses(String announceQueueName) throws JMSException {
		this.announceQueue = this.session.createQueue(announceQueueName);
		this.announceResponseQueue = this.session.createTemporaryQueue();
		this.announceMessageProducer = this.session.createProducer(this.announceQueue);
		this.announceResponseMessageConsumer = this.session.createConsumer(this.announceResponseQueue);
		Message message = this.session.createMessage();
		message.setJMSReplyTo(this.announceResponseQueue);
		this.announceMessageProducer.send(message);
		TextMessage responseMessage = (TextMessage)this.announceResponseMessageConsumer.receive();
		this.GameUUID = responseMessage.getText();
		System.out.println("Found by partner got game UUID: " + this.GameUUID);
		this.isMatched=true;
		this.announceResponseMessageConsumer.close();
		this.announceMessageProducer.close();
	}
}
