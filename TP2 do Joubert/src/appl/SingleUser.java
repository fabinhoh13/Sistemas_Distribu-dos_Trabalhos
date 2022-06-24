package appl;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Scanner;

import java.io.Console;
import java.io.IOException;
import java.lang.Runtime;

import core.Message;

public class SingleUser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new SingleUser();
	}
	
	public SingleUser(){
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.print("Enter the Broker port number: ");
		int brokerPort = reader.nextInt();
		
		System.out.print("Enter the Broker address: ");
		String brokerAdd = reader.next();
		
		System.out.print("Enter the User name: ");
		String userName = reader.next();
		
		System.out.print("Enter the User port number: ");
		int userPort = reader.nextInt();
		
		System.out.print("Enter the User address: ");
		String userAdd = reader.next();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PubSubClient user = new PubSubClient(userAdd, userPort);
		
		user.subscribe(brokerAdd, brokerPort);
		
		startTP2(user, userName, brokerPort, brokerAdd);
	}
	
	private void startTP2 (PubSubClient user, String userName, int brokerPort, String brokerAdd){
		String[] resources = {"var X", "var Y", "var Z"};
		
		Random seed = new Random();
		
		for(int i =0; i<100; i++){
			//fazendo um pub no broker
			String oneResource = resources[seed.nextInt(resources.length)];
			Thread sendOneMsg = new ThreadWrapper(user, userName+":"+oneResource, brokerAdd, brokerPort);
			
			sendOneMsg.start();
			
			try{
				sendOneMsg.join();			
			}catch (Exception e){
				e.printStackTrace();
			}
			
			//fazendo a obtencao dos notifies do broker
			List<Message> logUser = user.getLogMessages();
			
			treatLog(logUser);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
						
		user.unsubscribe(brokerAdd, brokerPort);
		
		user.stopPubSubClient();
		
	}
	
	private void treatLog(List<Message> logUser){
		//aqui existe toda a l�gica do protocolo do TP2
		//se permanece neste m�todo at� que o acesso a VAR X ou VAR Y ou VAR Z ocorra
		Iterator<Message> it = logUser.iterator();
		System.out.print("Log User itens: ");
		while(it.hasNext()){
			Message aux = it.next();
			System.out.print(aux.getContent() + aux.getLogId() + " | ");
		}
		System.out.println();
	}
	
	
	class ThreadWrapper extends Thread{
		PubSubClient c;
		String msg;
		String host;
		int port;
		
		public ThreadWrapper(PubSubClient c, String msg, String host, int port){
			this.c = c;
			this.msg = msg;
			this.host = host;
			this.port = port;
		}
		public void run(){
			c.publish(msg, host, port);
		}
	}

}
