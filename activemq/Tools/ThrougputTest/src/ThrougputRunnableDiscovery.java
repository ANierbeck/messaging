import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.UDPBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;



/**
 * 
 * @author tobias
 *
 */
public class ThrougputRunnableDiscovery implements Runnable {
	
	/* set this to the IP address which is in the broker network (for udp broadcast) */
	private static final String LOCAL_LISTEN_IP = "10.0.3.1";
	
	/* the UDP broadcast IP */
	private static final String UDP_GROUP_ADDRESS = "231.7.7.7";
	
	/* the UDP broadcast port */
	private static final int UDP_GROUP_PORT = 9876; 
	
	private Connection connection;
	private Session session;
	private MessageProducer producer;
	private MessageConsumer consumer;
	private TextMessage message; 
	
	/* create a randomly unique named queue for each test run */
	private Queue queue = ActiveMQJMSClient.createQueue("queue.performance.througput");
	
	private static final String message1KB = new String(new byte[Throughput.MESSAGE_SIZE_BYTE]);
	
	
		
	public ThrougputRunnableDiscovery() throws Exception {
		
		
		
	
		/* create a UDP discovery group */
		UDPBroadcastEndpointFactory udpCfg = new UDPBroadcastEndpointFactory();
		udpCfg
			.setGroupAddress(UDP_GROUP_ADDRESS)
			.setGroupPort(UDP_GROUP_PORT)
			.setLocalBindAddress(LOCAL_LISTEN_IP);
		
		DiscoveryGroupConfiguration groupConfiguration = new DiscoveryGroupConfiguration();
		groupConfiguration.setBroadcastEndpointFactory(udpCfg);
		
		ActiveMQConnectionFactory cf = ActiveMQJMSClient.createConnectionFactoryWithHA(groupConfiguration, JMSFactoryType.CF);
		
		/* these properties are required, otherwise the failover will NOT work !! */
		cf.setRetryInterval(1000);
		cf.setRetryIntervalMultiplier(1.5);
		cf.setMaxRetryInterval(60000);
		cf.setReconnectAttempts(1000);
		
		/* create a connection */
		connection = cf.createConnection("admin", "admin");        
		connection.start();
		
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		producer = session.createProducer(queue);
		if(Throughput.READ_MESSAGES) {
			consumer = session.createConsumer(queue);
		}
		message = session.createTextMessage(message1KB);
	}
	
	@Override
	public void run() {
		
		try {

			/* endless repetition of send-receive */
			for(int i=1; i<=Throughput.MESSAGES_PER_THREAD; i++) {
								
				/* create a text message and send it to the broker */
				
				producer.send(message);

				if(Throughput.READ_MESSAGES) {
					/* read the message from the server */
					TextMessage received = (TextMessage)consumer.receive();
				}
			}
			
			Throughput.setEnd(new Date());
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
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
