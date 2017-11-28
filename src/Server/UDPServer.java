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
        server.listenAndServe(mainPort);
    }

    private void listenAndServe(int port) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
//            logger.info("EchoServer is listening at {}", channel.getLocalAddress());
            System.out.println("EchoServer is listening at {} "+ channel.getLocalAddress());
            
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                buf.clear();
                SocketAddress router = channel.receive(buf);
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
                				continue;
                			}
//                			lastPort++;
                			System.out.println("Threeway handshake 1/3 with "+ packet.getPeerAddress());
							
//                			payload = "port:"+lastPort;
							//send ACK+SYN packet
                			int ACK_NUM = packet.getSeqN() + 1;
                			int SYNC_NUM = ThreadLocalRandom.current().nextInt(1, 5000);
							System.out.println("Here!!!");
                			Packet resp = packet.toBuilder()
                					 .setAckFlag(true)
                					 .setAckN(ACK_NUM)
                					 .setSynFlag(true)
                					 .setSeqN(SYNC_NUM)
                                     .setPayload(payload.getBytes())
                                     .create();
                               channel.send(resp.toBuffer(), router);
                               System.out.println("here1");
//							clientchannel.bind(new InetSocketAddress(lastPort));
							System.out.println("EchoServer is listening to a new client at {} "+ channel.getLocalAddress());
            	            ClientHandler newCli = new ClientHandler(lastPort, resp, channel, router);
            	            clients.add(newCli);
            	            Thread cliListener = new Thread(newCli);
            	            cliListener.start();
//            	    	} catch(IOException e) {
            	            	
//            	        }
                	}
                		
                }else if(state == State.FULL){
                	//reverse the packet and response the server cannot give service to client at this time...
                }
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
        	System.out.println("Cannot bind socket to port "+port);
			e.printStackTrace();
			return;
		}
        	
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
