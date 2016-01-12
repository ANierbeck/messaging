import java.math.BigInteger;
import java.security.SecureRandom;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.UDPBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.core.client.FailoverEventListener;
import org.apache.activemq.artemis.api.core.client.FailoverEventType;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.jms.client.ActiveMQConnection;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;



/**
 * 
 * @author tobias
 *
 */
public class ProducerRunnable implements Runnable {
	
	/* set this to the IP address which is in the broker network (for udp broadcast) */
	private static final String LOCAL_LISTEN_IP = "10.0.3.1";
	
	/* the UDP broadcast IP */
	private static final String UDP_GROUP_ADDRESS = "231.7.7.7";
	
	/* the UDP broadcast port */
	private static final int UDP_GROUP_PORT = 9876; 
	
	private Connection connection;
	private Session session;
	
	/* create a randomly unique named queue for each test run */
	private Queue queue = ActiveMQJMSClient.createQueue("test.test." + 
			new BigInteger(130, new SecureRandom()).toString(32));
		
	public ProducerRunnable() throws Exception {
	
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
		
		/* wait a short period for discovery */
		System.out.println("waiting for discovery");
		Thread.sleep(1000);
		
		/* create a connection */
		connection = cf.createConnection("admin", "admin");
		
		/* add a failover listener to see the failover steps */
        ((ActiveMQConnection) connection).setFailoverListener(new FailoverListenerImpl());
        
		connection.start();
	}
	
	@Override
	public void run() {
		
		try {

			int counter = 0;
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
			MessageProducer producer = session.createProducer(queue);
			MessageConsumer consumer = session.createConsumer(queue);
			
			/* endless repetition of send-receive */
			while(true) {
				
				String messageText = "msg_" + counter;
				
				System.out.print("sending: " + messageText + " ");
				
				/* create a text message and send it to the broker */
				TextMessage message = session.createTextMessage(messageText);	
				producer.send(message);
			
				/* wait a short time */
//				Thread.sleep(1000);
				
				/* read the message from the server */
				TextMessage received = (TextMessage)consumer.receive();
				
				
//				--------------------------------------------------------------------------------------------------------------
				
				/* extract the content of the received message */
				String resp = received.getText();

				System.out.print("expecting: " + messageText + " ");
				/* compare the received message with the expected one */
				if(resp.equals(messageText)) {
					System.out.println("OK");
				} else {
					System.out.println("received: " + messageText);
					
					/* exit if message is wrong */
					throw new Exception("RECEIVED WRONG MESSAGE!!!");
					
				}
				
				Thread.sleep(200);
				++counter;
			}
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

	
	/**
	 * Failover Listener which will alert all failover events
	 * @author tobias
	 *
	 */
	private static class FailoverListenerImpl implements FailoverEventListener {

		public void failoverEvent(FailoverEventType eventType) {
			System.out.println("Failover event triggered :" + eventType.toString());
		}
	}
	
}
