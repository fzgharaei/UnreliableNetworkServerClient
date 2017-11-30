package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import PacketLib.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

//https://github.com/pilosoposerio/tcp-in-udp/blob/master/Server.java
//https://github.com/michellefish/UDP-SelectiveRepeat/blob/master/UDP-SelectiveRepeat/src/UDPServer.java
public class UDPServer {
	final static int PACKET_SIZE = 512;
    final static int HEADER_SIZE = 118;
    final static int WINDOW_SIZE = 8;
    static int mainPort;
    static int lastPort;
    DatagramChannel channel;
    SocketAddress router;
    private static State state = State.NONE;
    static ArrayList<ClientHandler> clients;
    static int[] window;
    static int startWindow;
    static int numberOfTimeouts;
    public static void main(String[] args) throws IOException {
    	ServerOptionParser parser = new ServerOptionParser();
        parser.parse(args);
        mainPort = Integer.parseInt((String) parser.valueOf("port"));
        lastPort = mainPort;
        UDPServer server = new UDPServer();
        clients = new ArrayList<ClientHandler>();
        state = State.NONE;
        try{
        	server.listenAndServe(mainPort);
        }catch (IOException e) {
			// TODO Auto-generated catch block
        	System.out.println("Cannot bind socket to port "+mainPort);
			e.printStackTrace();
        }
    }

    private void listenAndServe(int port) throws IOException {

        this.channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(port));
//            logger.info("EchoServer is listening at {}", channel.getLocalAddress());
            System.out.println("EchoServer is listening at {} "+ channel.getLocalAddress());
            
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

//            for (; ; ) {
                buf.clear();
                router = channel.receive(buf);
                System.out.println(buf.toString());
                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                String payload = new String(packet.getPayload(), UTF_8);
                System.out.println("Packet:  "+ packet);
                System.out.println("Payload: "+ payload);
                System.out.println("Router:  "+ router);

                if(state == State.NONE){
                	if(packet.toBuilder().hasSynFlag() && !packet.toBuilder().hasAckFlag()){
//                		try(DatagramChannel clientchannel = DatagramChannel.open() ) {
                			if ((lastPort - mainPort) > 10){
                				state = State.FULL;
//                				continue;
                			}
//                			lastPort++;
                			System.out.println("Threeway handshake 1/3 with "+ packet.getPeerAddress());
							
//                			payload = "port:"+lastPort;
							//send ACK+SYN packet
                			System.out.println(packet.getSeqN());
                			int ACK_NUM = packet.getSeqN();
                			int SYNC_NUM = ThreadLocalRandom.current().nextInt(1, 256);
							System.out.println("Here!!!");
                			Packet resp = packet.toBuilder()
                					.setSynFlag(true)
            	        			.setAckFlag(true)
            	        			.setDataFlag(false)
            	        			.setFinFlag(false)
                					.setAckN(ACK_NUM)
                					.setSeqN(SYNC_NUM)
                                    .setPayload(payload.getBytes())
                                    .setPeerAddress(packet.getPeerAddress())
                                    .setPortNumber(packet.getPeerPort())
                                    .create();
                               channel.send(resp.toBuffer(), router);
                               System.out.println("here1");
//							clientchannel.bind(new InetSocketAddress(lastPort));
							System.out.println("EchoServer is listening to a new client at {} "+ channel.getLocalAddress());
            	            ClientHandler newCli = new ClientHandler(lastPort, resp);
            	            clients.add(newCli);
            	            Thread cliListener = new Thread(newCli);
            	            cliListener.start();
//            	    	} catch(IOException e) {
            	            	
//            	        }
                	}
                		
                }else if(state == State.FULL){
                	//reverse the packet and response the server cannot give service to client at this time...
                }
//            }
        
        	
    }
    private static Thread timerThread(final int seconds){
		return new Thread(new Runnable(){
			@Override
			public void run(){
				try{

					Thread.sleep(seconds*1000);
				}catch(InterruptedException ie){

				}
			}
		});
}
    class ClientHandler implements Runnable{
    	final int PACKET_SIZE = 1024;
        final int HEADER_SIZE = 11;
        final int WINDOW_SIZE = 8;
        private State state = State.NONE;
    	int[] window;
        int startWindow;
        int numberOfTimeouts;
//        DatagramChannel channel;
//        SocketAddress router;
        final int port;
        int cliSeqN;
        int cliAckN;
        Packet infoPacket;
        ClientHandler(int port, Packet initial){
        	this.port = port;
        	state = State.SYN_RECV;
        	System.out.println("Threeway handshake 2/3 with" + initial.getPeerAddress() +":"+ initial.getPeerPort());
        	infoPacket = initial;
//        	this.channel = channel;
//        	this.router = router;
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
//    		System.out.println("here2");
    		state = State.SYN_RECV;
    		ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);
    		System.out.println("here2.1");
    		while(true){
//    			System.out.println("here2.2");
    			buf.clear();
    			router = channel.receive(buf);
    			buf.flip();
                Packet packet = Packet.fromBuffer(buf);
//                System.out.println("here2.3");
//                System.out.println(packet);
                buf.flip();
//                System.out.println("here2.5");
                // some time managment should happen here!
    			if(state == State.SYN_RECV){
//    				System.out.println("here3");
    				
    				if(packet.toBuilder().hasAckFlag()){
//    					System.out.println("here4");
//    					System.out.println(packet.getAckN());
//    					System.out.println(cliSeqN);
//    					System.out.println(packet.getSeqN());
//    					System.out.println(cliAckN);
    					if(packet.getAckN() == (cliSeqN) && packet.getSeqN()==(cliAckN+1) ){
//    						System.out.println("here5");
    					
    					state = State.ESTABLISHED;
    					System.out.println("Threeway handshake 3/3.");
    					
    					}
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
}





//private static class PacketTimeout extends TimerTask{
//private int seq;
//private byte[] message;
//
//public PacketTimeout(int seq, byte[] message){
// this.seq = seq;
// this.message = message;
//}
//
//public void run(){
// //if packet has not been ACKed
// if(window[seq] == 0){
//    System.out.println("***PACKET TIMEOUT (seq: "+seq+")***");
//    numberOfTimeouts++;
//    try{
//       sendPacket(seq, message);
//    }
//       catch (Exception e){}
// }
//}
//}
