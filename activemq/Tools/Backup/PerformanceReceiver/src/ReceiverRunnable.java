import java.util.Date;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class ReceiverRunnable implements Runnable {
	
	private int hostId;
	private String url;
	private Connection connection;
	private ActiveMQConnectionFactory cf;
	private Session session;
	private MessageConsumer consumer;
	
	public ReceiverRunnable(int hostId, String url) throws Exception {
		this.hostId = hostId;
		this.url = url;
		
		cf = new ActiveMQConnectionFactory(this.url);
		connection = cf.createConnection(Main.USERNAME, Main.PASSWORD);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(Main.QUEUE);
		
		System.out.println("host" + hostId + " finished setup.");
	}

	
	
	public void run() {
		System.out.println("Starting receiver for server" + hostId);
		
		boolean started = false;
		boolean finished = false;
		Message msg;
		int msgCount = 0;
		
		try {
			
			connection.start();
			
			/* before the first messages arrive */
			while(!started) {
				msg = consumer.receive(200);

				/* start if the first message is not null */
				if(msg != null) {
					System.out.println("Started listener" + hostId);
					started = true;
				}
			}
			
			long start = System.currentTimeMillis();
		
			/* after the first message arrives until there are no more messages */
			while(!finished) {
				msg = consumer.receive(200);
				
				Main.printResult(hostId, finished, start, 0, msgCount);
				
				if(msg == null) {
					System.out.println("Finished listener " + hostId);
					finished = true;
				} else {
					++msgCount;
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
		
		
		System.out.println("Stopped receiver for server" + hostId);
	}

}
