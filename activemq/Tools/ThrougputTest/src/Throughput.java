import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;



public class Throughput {
	
	public static Date start;
	public static Date end;

//	TEST SETUP PARAMETERS
	
	/* message string size in bytes */
	public static int MESSAGE_SIZE_BYTE;
	
	/* amount of messages to send per thread */
	public static int MESSAGES_PER_THREAD;
	
	/* number of threads to execute */
	public static int THREAD_COUNT;
	
	public static final Properties props = new Properties();
	
	
	public static void main(String[] args) throws Exception {
		
		props.load(new FileInputStream("config.txt"));
		
		MESSAGE_SIZE_BYTE 	= Integer.parseInt(Throughput.props.getProperty("messageSizeBytes"));
		MESSAGES_PER_THREAD = Integer.parseInt(Throughput.props.getProperty("messagesPerThread"));
		THREAD_COUNT 		= Integer.parseInt(Throughput.props.getProperty("threadCount"));
		 		
		
				
		System.out.println("starting up");
		
		CleanUp cl = new CleanUp();
		cl.cleanUp();
		
		if(Throughput.props.getProperty("cleanupOnly").equals("true")) {
			System.out.println("Exiting because cleanup only mode is configured");
			System.exit(1);
		}
		
		ThrougputRunnable[] runners = new ThrougputRunnable[THREAD_COUNT];
		Thread[] threads = new Thread[THREAD_COUNT];
		
		for(int i=0; i<THREAD_COUNT; i++) {
			runners[i] = new ThrougputRunnable();
			threads[i] = new Thread(runners[i]);
		}
		
		System.out.println("Fire!");
		
		Throughput.start = new Date();
		
		for(int i=0; i<THREAD_COUNT; i++) {
			threads[i].start();
		}

		System.out.println("sending " + MESSAGES_PER_THREAD + " messages per thread with " + THREAD_COUNT + " threads");
		
	}
	
	public static synchronized void setEnd(Date ende) {
		if(Throughput.end == null || ende.getTime() > Throughput.end.getTime()) {
			System.out.println("updating end time");
			Throughput.end = ende;
			
			System.out.println("Laufzeit: " + (Throughput.end.getTime() - Throughput.start.getTime()));
			System.out.println("Durchsatz: " + (MESSAGES_PER_THREAD*THREAD_COUNT)/((Throughput.end.getTime() - Throughput.start.getTime())/1000) + " Nachrichten pro Sekunde");
		}
	}
	

}
