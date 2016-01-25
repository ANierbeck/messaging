import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class SenderRunnable implements Runnable {
	
	private int hostId;
	private String url;
	private Connection connection;
	private ActiveMQConnectionFactory cf;
	private Session session;
	private MessageProducer producer;
	
	//TODO: RANDOM MESSAGE VERWENDEN ANSTATT EMPTY MESSAGE
	
	private static final String message = new String(
			new byte[Integer.parseInt(Main.props.getProperty("messageSizeBytes"))]);
	
	public SenderRunnable(int hostId, String url) throws Exception {
		this.hostId = hostId;
		this.url = url;
		
		cf = new ActiveMQConnectionFactory(this.url);
		cf.setRetryInterval(1000);
		cf.setRetryIntervalMultiplier(1.5);
		cf.setMaxRetryInterval(60000);
		cf.setReconnectAttempts(1000);
		
		connection = cf.createConnection(Main.USERNAME, Main.PASSWORD);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(Main.QUEUE);
		
		System.out.println("host" + hostId + " finished setup.");
	}

	
	
	public void run() {
		System.out.println("Starting sender" + hostId);
		
		boolean finished = false;
		int msgCount = 0;
		
		try {
			
			TextMessage msg = session.createTextMessage(SenderRunnable.message);
			connection.start();
			
			long start = System.currentTimeMillis();
			
			while(!finished) {
				
				producer.send(msg);
				
				Main.printResult(hostId, finished, start, 0, msgCount);
				
				if(msgCount >= Main.MESSAGES_PER_THREAD) {
					finished = true;
				}
			}
			
			long end = System.currentTimeMillis();
			Main.printResult(hostId, finished, start, end, msgCount);

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
	
}

