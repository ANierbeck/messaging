import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.jms.Queue;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

public class Receiver {
	
	public static final Properties props = new Properties();
	
	/* list of servers */
	public static ArrayList<ReceiverRunnable> servers = new ArrayList<ReceiverRunnable>();
	
	public static String USERNAME;
	public static String PASSWORD;
	public static Queue  QUEUE;
	
	/* final statistics */
	private static int  allMsgCount;
	private static long allStart;
	private static long allEnd;
	
	/* for every Nth message the intermediate results will be printed */
	public static int INTERMEDIATE_INTERVAL;
	
	public static void main(String[] args) {
		
		/* load config.txt properties */
		try {
			props.load(new FileInputStream("receiver.txt"));
			
			Receiver.PASSWORD = props.getProperty("password");
			Receiver.USERNAME = props.getProperty("username");
			Receiver.QUEUE    = ActiveMQJMSClient.createQueue(props.getProperty("queueName"));
			Receiver.INTERMEDIATE_INTERVAL = 
					Integer.parseInt(props.getProperty("intermediateInterval", "1000"));
			
		} catch(Exception e) {
			System.out.println("Error reading config.txt properties file");
			e.printStackTrace();
		}

		/* read server list from config.txt */
		for(int i=0; i<100; i++) {
			String server = props.getProperty("server" + i);
			if(server != null) {
				
				try {
					ReceiverRunnable rec = new ReceiverRunnable(i, server);
					servers.add(rec);
				} catch(Exception e) {
					e.printStackTrace();
				}

				System.out.println("Added server" + i + ": " + server);
			}
		}
		
		/* start all receiver threads */
		for(ReceiverRunnable rec: servers) {
			rec.getThread().start();
		}
		
		boolean allFinished = false;
		
		while(!allFinished) {
			try {
				Thread.sleep(1000);
			} catch(Exception e) {
				allFinished = true;
			}
			
			allFinished = true;
			for(ReceiverRunnable rec: servers) {
				if(rec.getThread().isAlive()) {
					allFinished = false;
				}
			}
		}
		
		printEndReport();
	}
	
	private static void printEndReport() {
		long througput = (Receiver.allMsgCount*1000/(allEnd - allStart));
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("FINAL REPORT :");
		System.out.println(througput + " msg/sec");
		System.out.println(Receiver.allMsgCount + " messages");
		System.out.println("----------------------------------------------------------------------");
		
	}

	/**
	 * prints out the current results
	 * @param receiverId
	 * @param finished
	 * @param start
	 * @param end
	 * @param msgCount
	 */
	public static synchronized void printResult(int receiverId, boolean finished, long start, long end, int msgCount) {

		/* set the start for the first report */
		if(Receiver.allStart == 0) {
			Receiver.allStart = start; 
		}
		
		if(finished || msgCount % INTERMEDIATE_INTERVAL == 0) {
			
			/* if not yet finished */
			if(end == 0) end = System.currentTimeMillis();
			
			long elapsedTime = (end - start);
			
			if(finished) {
				System.out.print("FINAL RESULT: ");
				System.out.println(receiverId);
				Receiver.allEnd = end;
				Receiver.allMsgCount += msgCount;
			}
			
			long througput = 0;
			if(elapsedTime != 0) througput = (msgCount*1000/(elapsedTime));
			
			System.out.println("server"+receiverId + " says: " + msgCount + " in " + elapsedTime + "ms. Thats " + througput + " msg/sec");
		}
	}
}

