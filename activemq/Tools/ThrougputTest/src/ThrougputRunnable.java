import java.util.Date;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;



/**
 * 
 * @author tobias
 *
 */
public class ThrougputRunnable implements Runnable {
	
	private Connection connection;
	private Session session;
	private MessageProducer producer;
	private TextMessage message; 
	
	/* create a randomly unique named queue for each test run */
	private Queue queue = ActiveMQJMSClient.createQueue(Throughput.props.getProperty("queueName"));
	
	private static final String message1KB = new String(new byte[Throughput.MESSAGE_SIZE_BYTE]);
	
	
		
	public ThrougputRunnable() throws Exception {
			
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(Throughput.props.getProperty("connectTo"));
		
		/* these properties are required, otherwise the failover will NOT work !! */
		cf.setRetryInterval(1000);
		cf.setRetryIntervalMultiplier(1.5);
		cf.setMaxRetryInterval(60000);
		cf.setReconnectAttempts(1000);
		
		/* create a connection */
		connection = cf.createConnection(Throughput.props.getProperty("username"), Throughput.props.getProperty("password"));        
		connection.start();
		
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		producer = session.createProducer(queue);
		message = session.createTextMessage(message1KB);
	}
	
	@Override
	public void run() {
		
		try {

			/* endless repetition of send-receive */
			for(int i=1; i<=Throughput.MESSAGES_PER_THREAD; i++) {
								
				/* create a text message and send it to the broker */
				
				producer.send(message);
			}
			
			Throughput.setEnd(new Date());
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch(Exception e) {
				System.out.println("error closing connection: " + e);
			}
			close();
		}
	}
	
	
	/**
	 * close connection and stuff
	 */
	public void close() {
		try {
			connection.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
