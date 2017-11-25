package Server;
import java.io.IOException;
import java.nio.channels.DatagramChannel;

import PacketLib.*;

public class ClientHandler implements Runnable{
	final static int PACKET_SIZE = 512;
    final static int HEADER_SIZE = 118;
    final static int WINDOW_SIZE = 8;
    private static State state = State.NONE;
	static int[] window;
    static int startWindow;
    static int numberOfTimeouts;
    DatagramChannel channel;
    
	@Override
	public void run() {
		try {
			System.out.println("Thread started with name:"
					+ Thread.currentThread().getName());
			serve();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	public void serve() throws IOException, InterruptedException{
		try {
		} catch (Exception e) {
		}
		
	}
    
}
