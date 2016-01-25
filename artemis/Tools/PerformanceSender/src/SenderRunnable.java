import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class SenderRunnable implements Runnable {
	
	private Thread thread;
	private int hostId;
	private String url;
	private Connection connection;
	private ActiveMQConnectionFactory cf;
	private Session session;
	private MessageProducer producer;
	private static final String message;
	
	static {
		
		/* create random text message */
	    StringBuffer sb = new StringBuffer();  
	    for (int i = 0; i < Sender.MESSAGE_SIZE_BYTES; i++)  
	    {  
	      sb.append((char)((int)(Math.random()*26)+97));  
	    }
	    
	    message = sb.toString();
	}
	
	public SenderRunnable(int hostId, String url) throws Exception {
		thread = new Thread(this);
		this.hostId = hostId;
		this.url = url;
		
		cf = new ActiveMQConnectionFactory(this.url);
		cf.setRetryInterval(1000);
		cf.setRetryIntervalMultiplier(1.5);
		cf.setMaxRetryInterval(60000);
		cf.setReconnectAttempts(1000);
		
		connection = cf.createConnection(Sender.USERNAME, Sender.PASSWORD);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(Sender.QUEUE);
		
		System.out.println("host" + hostId + " finished setup.");
	}

	
	
	public void run() {
		System.out.println("Starting sender" + hostId);
		
		boolean finished = false;
		int msgCount = 0;
		
		try {
			
			TextMessage msg = session.createTextMessage(SenderRunnable.message);
			
			if(Sender.PERSISTENT) {
				msg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
			} else {
				msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			}
			
			connection.start();
			
			long start = System.currentTimeMillis();
			
			while(!finished) {
				++msgCount;
				try {
					producer.send(msg);
				} catch(Exception e) {
					System.out.println("KONNTE NACHRICHT NICHT SENDEN!");
				}
				
				Sender.printResult(hostId, finished, start, 0, msgCount);
				
				if(msgCount >= Sender.MESSAGES_PER_THREAD) {
					finished = true;
				}
			}
			
			long end = System.currentTimeMillis();
			Sender.printResult(hostId, finished, start, end, msgCount);

		} catch(Exception e) {
			System.out.println("Exception in receiver loop");
			e.printStackTrace();
		} finally { 
			try {
				connection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
	System.out.println("Stopped sender for server" + hostId);
	
	}
	
	public Thread getThread() {
		return thread;
	}
	
}

