package Client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import PacketLib.*;


import static java.nio.channels.SelectionKey.OP_READ;
//https://github.com/michellefish/UDP-SelectiveRepeat/blob/master/UDP-SelectiveRepeat/src/UDPClient.java
//https://github.com/pilosoposerio/tcp-in-udp/blob/master/Server.java
public class UDPClient {
	private static final int MAXIMUM_BUFFER_SIZE = 1024;
	private static UDPClientCmdParser inputParser;
	private static UDP_Request request;
	private static State state= State.NONE;
	private static int ACK_NUM = 0;
	private static int SYNC_NUM = 0;
	private static DatagramChannel channel;
	private static int INITIAL_SEGMENT = 0;
	private static SocketAddress senderRouterAddress;
    
    private static void runClient(SocketAddress senderRouterAddress,Packet p) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
           
        	channel.send(p.toBuffer(), senderRouterAddress);

//            logger.info("Sending \"{}\" to router at {}", msg, routerAddr);

            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
                return;
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            System.out.println(payload);
            keys.clear();
        }
    }

    public static void main(String[] args) throws Exception {
    	Scanner scanner = new Scanner(System.in);
    	ClientOptionParser parser = new ClientOptionParser();
    	//UDPClientCmdParser inputParser = new UDPClientCmdParser();
    	//UDP_Request request;
        parser.parse(args);

        // Router address
        String routerHost = (String) parser.valueOf("router-host");
        int routerPort = Integer.parseInt((String) parser.valueOf("router-port"));
        
        //should initiate the handshaking process and build the initial packets now
//        state = State.NONE;
        senderRouterAddress = new InetSocketAddress(routerHost, routerPort);
       
        listener.start();
		System.out.println("UDP listener for this client started!");
       
		Packet packet = makePacket(null, state);
		runClient(senderRouterAddress, packet);
		state = State.SYN_SEND;
		System.out.println("Threeway handshake 1/3.");
        
		}
    
	private static Thread listener = new Thread(new Runnable(){
		@Override 
		public void run() { 
			while(true){

				Packet responsePacket;
				ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
				try {
					
					SocketAddress receiverRouterAddress = channel.receive(buf);
					buf.flip();
		        
					responsePacket = Packet.fromBuffer(buf);
					String payload = new String(responsePacket.getPayload(), StandardCharsets.UTF_8);
					
					if(state == State.SYN_SEND && responsePacket.getAckN() == responsePacket.getSeqN() + 1){
						
				        	  System.out.println("Threeway handshake 2/3.");
				        	  
				        	  SYNC_NUM = responsePacket.getAckN();
				        	  ACK_NUM = responsePacket.getSeqN()+ 1;
				        	  
				        	  Packet ackPacket = makePacket(null, state);
				        	  
				        	  System.out.println("Threeway handshake 3/3.");
				        	  
				        	  runClient(senderRouterAddress, ackPacket);
				        	  
				        	  state = State.ESTABLISHED;
				        	  SYNC_NUM =  INITIAL_SEGMENT = ACK_NUM;
			        	 
				    }else if(state == State.ESTABLISHED){
						if(responsePacket.getSeqN() > SYNC_NUM){
							
						}
	//					String input = scanner.nextLine();
	//					packet = makePacket(input, state);
	//					packet = PacketWrapper.makePacket(null, state);
						
					}else if(state == State.FIN_RECV){
				        
			        }
						
				}
				catch (Exception e) {
						System.out.println("Failed to receive a packet... "+e.getMessage());
					} 
				} 
			} 
		}); 
    
    public static  Packet makePacket(String input, State state) throws Exception {
		try {
			request = inputParser.parse(input.split(" "));
			String serverHost = new URL(request.getUrl()).getHost();
			int serverPort = new URL(request.getUrl()).getPort();
	        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
	        Packet p;
	       
	        if(state == State.NONE)
	        {
	        	p =new Packet.Builder()
	        			.setSynFlag(true)
	        			.setAckFlag(false)
	        			.setType()
	        			.setSeqN(new Random().nextInt(65000))
	        			.setPortNumber(serverPort)
	        			.setPeerAddress(serverAddress.getAddress())
	        			.create();
	        	System.out.println("Threeway handshake 1/3.");
	        	state = State.SYN_SEND;
	        	return p;
	        }else if(state == State.SYN_SEND)
	        {
	        	
	        	p =new Packet.Builder()
	        			.setSynFlag(true)
	        			.setAckFlag(true)
	        			.setType()
	        			.setSeqN(SYNC_NUM)
	        			.setAckN(ACK_NUM)
	        			.setPortNumber(serverPort)
	        			.setPeerAddress(serverAddress.getAddress())
	        			.create();
	        	System.out.println("Threeway handshake 1/3.");
	        	state = State.ESTABLISHED;
	        	
	        	return p;
	        }else if(state == State.ESTABLISHED && input != null){
	        	p = new Packet.Builder()
	        			.setSynFlag(true)
	        			.setAckFlag(true)
	        			.setType()
	                    //.setAckN(ACK_NUM)
	                    //.setSeqN(SYNC_NUM)
	                    .setPortNumber(serverPort)
	                    .setPeerAddress(serverAddress.getAddress())
//	                    .setPayload(request.getRequestParameters().getData().getBytes())
	                    .create();
				
	        	return p;
	        }else if(state == State.FIN_RECV){
	        
	        }

		} catch (Exception e) {
			throw new Exception("ERROR Wrapping Packet");
		}
		return null;
		
		
	}
}


