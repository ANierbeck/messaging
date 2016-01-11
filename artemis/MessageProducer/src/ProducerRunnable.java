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




public class ProducerRunnable implements Runnable {
	
	private Connection connection;
	private Session session;
	private Queue queue = ActiveMQJMSClient.createQueue("test.test." + 
			new BigInteger(130, new SecureRandom()).toString(32));
		
	public ProducerRunnable() throws Exception {
	
	
		UDPBroadcastEndpointFactory udpCfg = new UDPBroadcastEndpointFactory();
		udpCfg
			.setGroupAddress("231.7.7.7")
			.setGroupPort(9876);
		
		DiscoveryGroupConfiguration groupConfiguration = new DiscoveryGroupConfiguration();
		groupConfiguration.setBroadcastEndpointFactory(udpCfg);
		
		ActiveMQConnectionFactory cf = ActiveMQJMSClient.createConnectionFactoryWithHA(groupConfiguration, JMSFactoryType.CF);
		cf.setRetryInterval(1000);
		cf.setRetryIntervalMultiplier(1.5);
		cf.setMaxRetryInterval(60000);
		cf.setReconnectAttempts(1000);
		
		System.out.println("waiting for discovery");
		Thread.sleep(2000);
		
		connection = cf.createConnection("admin", "admin");
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
			
			while(true) {
				
				String messageText = "msg_" + counter;
				
				System.out.println("sending message: " + messageText);
				
				TextMessage message = session.createTextMessage(messageText);	
				producer.send(message);
					
				System.out.println("receiving message...");	
				
				Thread.sleep(1000);
				
				TextMessage received = (TextMessage)consumer.receive();
				
				
//				--------------------------------------------------------------------------------------------------------------
				
//				System.out.println("recived: " + received);
				
				String resp = received.getText();
				
				System.out.println(resp);
				
				if(resp.equals(messageText)) {
					System.out.println("OK");
				} else {
					System.out.println(messageText);
					throw new Exception("RECEIVED WRONG MESSAGE!!!");
					
				}
				
				Thread.sleep(1000);
				++counter;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
			close();
		}
	}
	
	public void close() {
		try {
			connection.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	   private static class FailoverListenerImpl implements FailoverEventListener {

		      public void failoverEvent(FailoverEventType eventType) {
		         System.out.println("Failover event triggered :" + eventType.toString());
		      }
		   }
}
