
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class SenderRunnable implements Runnable {
	
	private Thread thread;
	private int hostId;
	private Connection connection;
	private ConnectionFactory cf;
	private Channel channel;
	private static final String message;
	
	static {
		
		/* create random text message */
	    StringBuffer sb = new StringBuffer();  
	    for (int i = 0; i < Sender.MESSAGE_SIZE_BYTES; i++)  
	    {  
	      sb.append((char)((int)(Math.random()*26)+97));
	    }
	    
	    message = sb.toString();
	}
	
	public SenderRunnable(int hostId, String hostname, int port) throws Exception {
		
		thread = new Thread(this);
		this.hostId = hostId;
		
		cf = new ConnectionFactory();
		cf.setUsername(Sender.USERNAME);
		cf.setPassword(Sender.PASSWORD);
		cf.setHost(hostname);

		connection = cf.newConnection();
		
		channel = connection.createChannel();
		
		channel.queueDeclare(Sender.props.getProperty("queueName"), false, false, false, null);
				
		System.out.println("host" + hostId + " finished setup.");
	}

	
	
	public void run() {
		System.out.println("Starting sender" + hostId);
		
		boolean finished = false;
		int msgCount = 0;
		
		try {
					
//			if(Sender.PERSISTENT) {
//				msg.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
//			} else {
//				msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
//			}
			
			long start = System.currentTimeMillis();
			
			while(!finished) {
				++msgCount;
				try {
					channel.basicPublish("", Sender.props.getProperty("queueName"), null, message.getBytes());
				} catch(Exception e) {
					System.out.println("KONNTE NACHRICHT NICHT SENDEN!");
				}
				
				Sender.printResult(hostId, finished, start, 0, msgCount);
				
				if(msgCount >= Sender.MESSAGES_PER_THREAD) {
					finished = true;
				}
			}
			
			long end = System.currentTimeMillis();
			Sender.printResult(hostId, finished, start, end, msgCount);

		} catch(Exception e) {
			System.out.println("Exception in receiver loop");
			e.printStackTrace();
		} finally { 
			try {
				channel.close();
				connection.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
	System.out.println("Stopped sender for server" + hostId);
	
	}
	
	public Thread getThread() {
		return thread;
	}
	
}

