package appl;

import core.Message;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import core.Host;

public class OneAppl {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new OneAppl(true);
	}

	public OneAppl(boolean flag){

		String hostPrimary = "localhost";
		int portPrimary = 8080;

		String hostBackup = "localhost";
		int portBackup = 8081;
		
		String hostClient = "localhost";

		Host primary = new Host("localhost", 8080);
		Host secondary = new Host("localhost", 8081);

		PubSubClient clientA = new PubSubClient(hostClient, 8087);
		PubSubClient clientB = new PubSubClient(hostClient, 8088);
		PubSubClient clientC = new PubSubClient(hostClient, 8089);
		
		clientA.subscribe(primary);
		clientB.subscribe(primary);
		clientC.subscribe(primary);
		
		clientA.subscribe(secondary);
		clientB.subscribe(secondary);
		clientC.subscribe(secondary);
		
		Thread accessOne = new requestAcquire(clientA, "ClientA",  "-acquire-", "X", primary);
		Thread accessTwo = new requestAcquire(clientB, "ClientB",  "-acquire-", "X", primary);
		Thread accessThree = new requestAcquire(clientC, "ClientC",  "-acquire-", "X", secondary);

		int seconds = (int) (Math.random()*(10000 - 1000)) + 1000;
		System.out.println("Starting in " + seconds/1000 + " seconds...\n");

		try { Thread.sleep(seconds);
		}catch (Exception ignored) {}

		accessOne.start();
		accessTwo.start();
		accessThree.start();
		
		try{
			accessOne.join();
			accessTwo.join();
			accessThree.join();
		}catch (Exception ignored){}

		clientA.unsubscribe(hostPrimary, portPrimary);
		clientB.unsubscribe(hostPrimary, portPrimary);
		clientC.unsubscribe(hostPrimary, portPrimary);
		
		clientA.stopPubSubClient();	
		clientB.stopPubSubClient();	
		clientC.stopPubSubClient();	
	}
}

class requestAcquire extends Thread {
	PubSubClient client;
	String clientName;
	String action;
	String resource;
	Host broker;

	public requestAcquire(PubSubClient client, String clientName, String action, String resource, Host broker) {
		this.client = client;
		this.clientName = clientName;
		this.action = action;
		this.resource = resource;
		this.broker = broker;
	}

	public void run() {	
		ThreadWrapper access = new ThreadWrapper(client, clientName.concat(action).concat(resource), broker);
		access.start();

		try {
			access.join();
		} catch (Exception ignored) {}
		

		List<Message> logs = client.getLogMessages();
		List<String> acquires = new ArrayList<String>();

		while(true){

			Iterator<Message> it = logs.iterator();
			while(it.hasNext()){
				Message log = it.next();
				String content = log.getContent();
				if (content.contains("-acquire-")){
					acquires.add(content);
				}
			}

			System.out.print("\nORDEM DE CHEGADA MANTIDA PELO BROKER: " + acquires + " \n");
			
			while (!acquires.isEmpty()){
				
				String firstClient = acquires.get(0);
				boolean hasRelease = false;
				
				while(!hasRelease){
					
					int randomInterval = getRandomInteger(1000, 10000);

					if(firstClient.startsWith(clientName)){
						
						access = new ThreadWrapper(client, "use"+resource, broker);
						access.start();
				
						try {
							access.join();
						} catch (Exception ignored) {}
						
						System.out.println("___________________________");
						System.out.println(firstClient.split("-")[0] + " pegou o recurso " + resource);

						System.out.println("Aguardando " + randomInterval/1000 + " segundos...\n");
						try {
							Thread.sleep(randomInterval);
						}catch (Exception ignored) {}
						
						access = new ThreadWrapper(client, clientName.concat("-release-").concat(resource), broker);
						access.start();
						hasRelease = true;

					}else{
						break;
					}
				}

				if (!acquires.isEmpty()){
					acquires.remove(0);
				}
			}
		}	
	}
	
	public int getRandomInteger(int minimum, int maximum){ 
		return ((int) (Math.random()*(maximum - minimum))) + minimum; 
	}

}

class ThreadWrapper extends Thread{
	PubSubClient c;
	String msg;
	Host broker;
	
	public ThreadWrapper(PubSubClient c, String msg, Host broker){
		this.c = c;
		this.msg = msg;
		this.broker = broker;
	}

	public void run(){
		c.publish(msg, broker);
	}

}