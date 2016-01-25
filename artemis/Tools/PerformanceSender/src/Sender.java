import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.jms.Queue;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;

public class Sender {
	
	public static final Properties props = new Properties();
	
	/* list of sender threads */
	public static ArrayList<SenderRunnable> senders  = new ArrayList<SenderRunnable>();

	public static String USERNAME;
	public static String PASSWORD;
	public static Queue QUEUE;
	
	public static int THREAD_COUNT;
	public static int MESSAGES_PER_THREAD;
	public static boolean PERSISTENT;
	public static int MESSAGE_SIZE_BYTES;
	
	/* for every Nth message the intermediate results will be printed */
	public static int INTERMEDIATE_INTERVAL;
	
	/* final statistics */
	private static int allMsgCount;
	private static long allStart;
	private static long allEnd;

	
	public static void main(String[] args) {
		
		/* load config.txt properties */
		try {
			props.load(new FileInputStream("sender.txt"));
			
			Sender.PASSWORD = props.getProperty("password");
			Sender.USERNAME = props.getProperty("username");
			Sender.QUEUE    = ActiveMQJMSClient.createQueue(props.getProperty("queueName"));
			Sender.THREAD_COUNT = Integer.parseInt(
					props.getProperty("threadCount"));
			Sender.MESSAGES_PER_THREAD = Integer.parseInt(
					props.getProperty("messagesPerThread"));
			Sender.INTERMEDIATE_INTERVAL = 
					Integer.parseInt(props.getProperty("intermediateInterval", "1000"));
			Sender.PERSISTENT = new Boolean(props.getProperty("persistent", "true"));
			System.out.println("persistence: " + Sender.PERSISTENT);
			Sender.MESSAGE_SIZE_BYTES = 
					Integer.parseInt(Sender.props.getProperty("messageSizeBytes"));
			
		} catch(Exception e) {
			System.out.println("Error reading config.txt properties file");
			e.printStackTrace();
		}

		String server = props.getProperty("server");

		/* create threads */
		for(int i=0; i<THREAD_COUNT; i++) {
			try {
				senders.add(new SenderRunnable(i, server));
				System.out.println("Adding sender" + i + ": " + server);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		/* start all sender threads */
		for(SenderRunnable sr : senders) {
			sr.getThread().start();
		}
		
		boolean allFinished = false;
		
		while(!allFinished) {
			try {
				Thread.sleep(1000);
			} catch(Exception e) {
				allFinished = true;
			}
			
			allFinished = true;
			for(SenderRunnable sr : senders) {
				if(sr.getThread().isAlive()) {
					allFinished = false;
				}
			}
		}
		
		printEndReport();
	}
	
	
	private static void printEndReport() {
		long througput = (Sender.allMsgCount*1000/(allEnd - allStart));
		
		System.out.println("----------------------------------------------------------------------");
		System.out.println("FINAL REPORT :");
		System.out.println(througput + " msg/sec");
		System.out.println(Sender.allMsgCount + " messages");
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
		if(Sender.allStart == 0) {
			Sender.allStart = start;
		}
		
		if(finished || msgCount % INTERMEDIATE_INTERVAL == 0) {
			
			/* if not yet finished */
			if(end == 0) end = System.currentTimeMillis();
			
			long elapsedTime = (end - start);
			
			if(finished) {
				System.out.print("FINAL RESULT: ");
				System.out.println(senderId);
				Sender.allEnd = end;
				Sender.allMsgCount = Sender.allMsgCount + msgCount;
			}
			
			long througput = 0;
			if(elapsedTime != 0) througput = (msgCount*1000/(elapsedTime));
			
			System.out.println("sender"+senderId + " says: " + msgCount + " in " + elapsedTime + "ms. Thats " + througput + " msg/sec");
		}
	}
}

