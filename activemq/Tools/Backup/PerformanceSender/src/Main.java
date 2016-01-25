import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.Queue;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

public class Main {
	
	public static final Properties props = new Properties();
	
	/* list of sender threads */
	public static Map<Integer, String>  servers  = new HashMap<Integer, String>();
	
	/* list, which sender threads have finished */
	public static Map<Integer, Boolean> finished = new HashMap<Integer, Boolean>();
	
	public static String USERNAME;
	public static String PASSWORD;
	public static Queue QUEUE;
	
	public static int THREAD_COUNT;
	public static int MESSAGES_PER_THREAD;
	
	/* for every Nth message the intermediate results will be printed */
	public static int INTERMEDIATE_INTERVAL;
	
	/* final statistics */
	private static int allMsgCount;
	private static long allStart;
	private static long allEnd;

	
	public static void main(String[] args) {
		
		/* load config.txt properties */
		try {
			props.load(new FileInputStream("config.txt"));
			
			Main.PASSWORD = props.getProperty("password");
			Main.USERNAME = props.getProperty("username");
			Main.QUEUE    = ActiveMQJMSClient.createQueue(props.getProperty("queueName"));
			Main.THREAD_COUNT = Integer.parseInt(
					props.getProperty("threadCount"));
			Main.MESSAGES_PER_THREAD = Integer.parseInt(
					props.getProperty("messagesPerThread"));
			Main.INTERMEDIATE_INTERVAL = 
					Integer.parseInt(props.getProperty("intermediateInterval", "1000"));
			
		} catch(Exception e) {
			System.out.println("Error reading config.txt properties file");
			e.printStackTrace();
		}

		String server = props.getProperty("server");

		/* create threads */
		for(int i=0; i<THREAD_COUNT; i++) {
				servers.put(i, server);
				finished.put(i, false);
				System.out.println("Adding server" + i + ": " + server);
		}
		
		/* init all sender connections */
		Map<SenderRunnable, Thread> senders = new HashMap<SenderRunnable, Thread>();
		
		try {
			for( int serverId : servers.keySet()) {
				SenderRunnable rec = new SenderRunnable(serverId, servers.get(serverId));
				senders.put(rec, new Thread(rec));
			}
			
		} catch(Exception e) {
			System.out.println("error initializing sender connections");
			e.printStackTrace();
		}
		
		/* start all sender threads */
		for(Thread thread : senders.values()) {
			thread.start();
		}
		
		boolean allFinished = false;
		
		while(!allFinished) {
			try {
				Thread.sleep(1000);
			} catch(Exception e) {
				allFinished = true;
			}
			
			allFinished = true;
			for(Boolean serverFinished : finished.values()) {
				if(!serverFinished) {
					allFinished = false;
				}
			}
		}
		
		printEndReport();
	}
	
	
	private static void printEndReport() {
		long througput = (Main.allMsgCount*1000/(allEnd - allStart));
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("FINAL REPORT :");
		System.out.println(througput + " msg/sec");
		System.out.println(Main.allMsgCount + " messages");
		System.out.println("----------------------------------------------------------------------");
		
	}

	/**
	 * prints out the current results
	 * @param senderId
	 * @param finished
	 * @param start
	 * @param end
	 * @param msgCount
	 */
	public static synchronized void printResult(int senderId, boolean finished, long start, long end, int msgCount) {

		/* set the start for the first report */
		if(Main.allStart == 0) {
			Main.allStart = start;
		}
		
		if(finished || msgCount % INTERMEDIATE_INTERVAL == 0) {
			
			/* if not yet finished */
			if(end == 0) end = System.currentTimeMillis();
			
			long elapsedTime = (end - start);
			
			if(finished) {
				System.out.print("FINAL RESULT: ");
				Main.finished.put(senderId, true);
				System.out.println(Main.finished.get(senderId));
				Main.allEnd = end;
				Main.allMsgCount = Main.allMsgCount + msgCount;
			}
			
			long througput = 0;
			if(elapsedTime != 0) througput = (msgCount*1000/(elapsedTime));
			
			System.out.println("server"+senderId + " says: " + msgCount + " in " + elapsedTime + "ms. Thats " + througput + " msg/sec");
		}
	}
}

