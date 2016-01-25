import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.artemis.api.core.DiscoveryGroupConfiguration;
import org.apache.activemq.artemis.api.core.UDPBroadcastEndpointFactory;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;


public class Receiver {
	
	/* set this to the IP address which is in the broker network (for udp broadcast) */
	private static final String LOCAL_LISTEN_IP = "10.0.3.1";
	private static final int LOCAL_LISTEN_PORT = 1557;
	
	/* the UDP broadcast IP */
	private static final String UDP_GROUP_ADDRESS = "231.7.7.7";
	
	/* the UDP broadcast port */
	private static final int UDP_GROUP_PORT = 9876;
	
	private static final Queue queue = ActiveMQJMSClient.createQueue("test.permanentsender");

	private static Connection connection;
	private static Session session;
	
	
	public static void main(String[] args) throws Exception {
		setup();
		
		System.out.println("starting listener");
		
		MessageConsumer consumer = session.createConsumer(queue);

		for(int i=1; i<=1000; i++) {
			
			TextMessage received = (TextMessage)consumer.receive();
			System.out.println("received message: " + received.getText());
			
			if(!received.getText().equals("msg_" + i)) {
				System.out.println("Reihenfolge verletzt");
				
			}
						
		}
		
		session.close();
		connection.close();
		
	}
		
	
	/**
	 * set up the connection to the broker
	 * @throws Exception
	 */
	public static void setup() throws Exception {
					
		/* create a UDP discovery group */
		UDPBroadcastEndpointFactory udpCfg = new UDPBroadcastEndpointFactory();
		udpCfg
			.setGroupAddress(UDP_GROUP_ADDRESS)
			.setGroupPort(UDP_GROUP_PORT)
			.setLocalBindPort(LOCAL_LISTEN_PORT)
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
        
		connection.start();
		
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

}
