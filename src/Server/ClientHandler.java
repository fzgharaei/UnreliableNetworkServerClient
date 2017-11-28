package Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import PacketLib.*;

public class ClientHandler implements Runnable{
	final int PACKET_SIZE = 1024;
    final int HEADER_SIZE = 11;
    final int WINDOW_SIZE = 8;
    private State state = State.NONE;
	static int[] window;
    static int startWindow;
    static int numberOfTimeouts;
    DatagramChannel channel;
    SocketAddress router;
    final int port;
    int cliSeqN;
    int cliAckN;
    Packet infoPacket;
    ClientHandler(int port, Packet initial, DatagramChannel channel, SocketAddress router){
    	this.port = port;
    	state = State.SYN_RECV;
    	System.out.println("Threeway handshake 2/3 with" + initial.getPeerAddress() +":"+ initial.getPeerPort());
    	infoPacket = initial;
    	this.channel = channel;
    	this.router = router;
    	this.cliAckN = initial.getAckN();
    	this.cliSeqN = initial.getSeqN();
    }
    
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
		System.out.println("here2");
		ByteBuffer buf = ByteBuffer
                .allocate(Packet.MAX_LEN)
                .order(ByteOrder.BIG_ENDIAN);
		while(true){
			buf.clear();
			router = channel.receive(buf);
			buf.flip();
            Packet packet = Packet.fromBuffer(buf);
            buf.flip();
            // some time managment should happen here!
			if(state == State.SYN_RECV){
				if(packet.toBuilder().hasAckFlag() && packet.toBuilder().getackN() == (++cliSeqN)){
					state = State.ESTABLISHED;
					System.out.println("Threeway handshake 3/3.");
					
				}
	        }else if(state == State.ESTABLISHED){
	    
	        }else if(state == State.FIN_SEND){
	        
	        }
		}
	}
	
	private byte[][] segmentation(String filename) throws Exception{
        FileInputStream filestream = new FileInputStream(new File(filename));
        int size = (int)Math.ceil((double)(filestream.available())/(PACKET_SIZE-HEADER_SIZE));
        byte[][] segmentedFile = new byte[size][PACKET_SIZE-HEADER_SIZE];
        for(int i = 0; i < size; i++){
           for(int j = 0; j < (PACKET_SIZE-HEADER_SIZE); j++){
              if(filestream.available() != 0)
                 segmentedFile[i][j] = (byte)filestream.read();
              else
                 segmentedFile[i][j] = 0;
           }
        }
        filestream.close();
        return segmentedFile;
}
    
}
