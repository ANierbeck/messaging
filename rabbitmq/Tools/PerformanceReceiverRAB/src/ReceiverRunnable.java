import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ReceiverRunnable implements Runnable {

	private int hostId;
	private Thread thread;
	private Connection connection;
	private ConnectionFactory cf;
	private Channel channel;
	private boolean started = false;
	private boolean finished = false;
	public long start;
	public long end;
	public int msgCount;

	public ReceiverRunnable(int hostId, String hostname, int port) throws Exception {
		this.thread = new Thread(this);
		this.hostId = hostId;

		cf = new ConnectionFactory();
		cf.setUsername(Receiver.USERNAME);
		cf.setPassword(Receiver.PASSWORD);
		cf.setHost(hostname);
		// cf.setPort(port);

		connection = cf.newConnection();
		channel = connection.createChannel();

		channel.queueDeclare(Receiver.props.getProperty("queueName"), false, false, false, null);

		System.out.println("host" + hostId + " finished setup.");
	}

	public void run() {
		System.out.println("Starting receiver for server" + hostId);

		started = false;
		finished = false;
		String msg;
		
		msgCount = 0;

		try {

			// /* before the first messages arrive */
			// while(!started) {
			// msg = consumer.receive(200);
			//
			// /* start if the first message is not null */
			// if(msg != null) {
			// System.out.println("Started listener" + hostId);
			// started = true;
			// }
			// }
			started = true;
			start = System.currentTimeMillis();

			Consumer consumer = new DefaultConsumer(channel) {

				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					Receiver.printResult(hostId, finished, start, 0, msgCount);
					
//					if(msgCount % 1000 == 0) System.out.println("msg" + msgCount);
					++msgCount;
				}

			};

			msg = channel.basicConsume(Receiver.props.getProperty("queueName"), true, consumer);
			
			/*
			 * after the first message arrives until there are no more messages
			 */
			while (!finished) {

				if (msgCount >= Receiver.MSG_COUNT) {
					System.out.println("Finished listener " + hostId);
					finished = true;
				}
				Thread.sleep(100);
			}

			long end = System.currentTimeMillis();
			
			Receiver.printResult(hostId, finished, start, end, msgCount);

		} catch (Exception e) {
			System.out.println("Exception in receiver loop");
			e.printStackTrace();
		} finally {
			try {
				channel.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Stopped receiver for server" + hostId);
	}

	public Thread getThread() {
		return thread;
	}

	public synchronized void setFinished() {
		finished = true;
	}

}
