import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestLatency {
	
	public static long start;
	public static long end;
	
	
	public static void main(String[] args) {
		
		System.out.println("Starte latency check");
		
		
		ConnectionFactory cf = new ConnectionFactory();
		cf.setUsername("admin");
		cf.setPassword("admin");
		cf.setHost("10.0.3.101");
		
		Connection connection = cf.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare("test.latency", false, false, false, null);
		
		System.out.println("Starte Messung");
		
		start = System.currentTimeMillis();
		
		String message = "This is a test message";
		
		System.out.println("sende");
		channel.basicPublish("","test.latency", null, message.getBytes());
		
		Consumer consumer = new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				end = System.currentTimeMillis();
				
				long diff = end - start;
				
				System.out.println("RECEIVED MSG: " + message);
				System.out.println(("LATENCY: " + diff));
			}
		};
		
		System.out.println("consume");
		channel.basicConsume("test.latency", true, consumer);
		
		Thread.sleep(10000);
		
		channel.close();
		connection.close();
	}

}
