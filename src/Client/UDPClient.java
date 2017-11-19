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
import java.util.Scanner;
import java.util.Set;

import PacketLib.Packet;
import PacketLib.Packet.Builder;

import static java.nio.channels.SelectionKey.OP_READ;
//https://github.com/michellefish/UDP-SelectiveRepeat/blob/master/UDP-SelectiveRepeat/src/UDPClient.java
public class UDPClient {

    
    private static void runClient(SocketAddress routerAddr,Packet p) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
           
        	channel.send(p.toBuffer(), routerAddr);

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
    	UDPClientCmdParser inputParser = new UDPClientCmdParser();
    	UDP_Request request;
    	PacketWrapper wrapper = new PacketWrapper();
    	Packet packet = null;

        parser.parse(args);

        // Router address
        String routerHost = (String) parser.valueOf("router-host");
        int routerPort = Integer.parseInt((String) parser.valueOf("router-port"));

        boolean getInputFromClient = true;
        
		while(getInputFromClient){
			
			try {
				String input = scanner.nextLine();
				packet = wrapper.makePacket(input);	
			} catch (Exception e) {
				throw new Exception("Wrong Input Syntax !! Try Again");
			}
			
		}

        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);

        runClient(routerAddress, packet);
    }
}

