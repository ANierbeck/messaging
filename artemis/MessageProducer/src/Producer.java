

public class Producer {
	
	public static void main(String[] args) throws Exception {
	
		System.out.println("Starting");
	
		ProducerRunnable runner = new ProducerRunnable();
		
		Thread thread = new Thread(runner);
		
		thread.start();
		
	}
	

}
