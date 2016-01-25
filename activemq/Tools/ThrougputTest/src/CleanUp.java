import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class CleanUp {
	
	public void cleanUp() {
		
		try {
		
			System.out.println("beginning clean up");
			
			Queue queue = ActiveMQJMSClient.createQueue(Throughput.props.getProperty("queueName"));
			
			ActiveMQConnectionFactory cf1 = new ActiveMQConnectionFactory(Throughput.props.getProperty("connectTo"));
			ActiveMQConnectionFactory cf2 = new ActiveMQConnectionFactory(Throughput.props.getProperty("host2"));
			
			Connection connection1 = cf1.createConnection(Throughput.props.getProperty("username"), Throughput.props.getProperty("password"));
			Connection connection2 = cf2.createConnection(Throughput.props.getProperty("username"), Throughput.props.getProperty("password"));
			connection1.start();
			connection2.start();
			
			Session session1 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Session session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	
			
			MessageConsumer consumer1 = session1.createConsumer(queue);
			MessageConsumer consumer2 = session2.createConsumer(queue);
			
			Message tm;
			
			int counter = 0;
	
			
			do {
				tm = consumer1.receive(300);
				System.out.println(tm);
				counter++;
				if(tm != null) tm.acknowledge();
			} while(tm != null);
				
			do {
				tm = consumer2.receive(100);
				System.out.println(tm);
				counter++;
				if(tm != null) tm.acknowledge();
			} while(tm != null);
			
			System.out.println("finished cleanup. cleaned " + counter + " messages!");
			
			connection1.close();
			connection2.close();
			
			Thread.sleep(3000);
		} catch(Exception e) {
			System.out.println("error cleaning up.");
		}
	}

}
