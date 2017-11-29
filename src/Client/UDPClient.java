package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Set;
import PacketLib.*;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {

//    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);

    private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
            String msg = "Hello World";
            Packet p = new Packet.Builder()
            		.setSynFlag(true)
        			.setAckFlag(false)
        			.setDataFlag(false)
        			.setFinFlag(false)
        			.setType()
        			.setSeqN(new Random().nextInt(256))
        			.setAckN(0)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);
            System.out.println("1/3");
//            logger.info("Sending \"{}\" to router at {}", msg, routerAddr);
            System.out.println("Sending "+  msg+" to router at "+ routerAddr);
            
            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
//            logger.info("Waiting for the response");
            System.out.println("Waiting for the response");
            
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
//                logger.error("No response after timeout");
            	System.out.println("No response after timeout");
                
                return;
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
//            logger.info("Packet: {}", resp);
//            logger.info("Router: {}", router);
            System.out.println("Packet:"+ resp);
            System.out.println("Router: "+ router);
            System.out.println("2/3");
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
//            logger.info("Payload: {}",  payload);
            System.out.println("Payload: "+  payload);
            int ackN = resp.getSeqN();
            int seqN = p.getSeqN() +1;
            p = new Packet.Builder()
            		.setSynFlag(true)
        			.setAckFlag(true)
        			.setDataFlag(false)
        			.setFinFlag(false)
        			.setType()
        			.setAckN(ackN)
        			.setSeqN(seqN)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);
            System.out.println("3/3");
            keys.clear();
        }
    }

    public static void main(String[] args) throws IOException {
//        OptionParser parser = new OptionParser();
//        parser.accepts("router-host", "Router hostname")
//                .withOptionalArg()
//                .defaultsTo("localhost");
//
//        parser.accepts("router-port", "Router port number")
//                .withOptionalArg()
//                .defaultsTo("3000");
//
//        parser.accepts("server-host", "EchoServer hostname")
//                .withOptionalArg()
//                .defaultsTo("localhost");
//
//        parser.accepts("server-port", "EchoServer listening port")
//                .withOptionalArg()
//                .defaultsTo("8007");
//
//        OptionSet opts = parser.parse(args);
//
//        // Router address
//        String routerHost = (String) opts.valueOf("router-host");
//        int routerPort = Integer.parseInt((String) opts.valueOf("router-port"));
//
//        // Server address
//        String serverHost = (String) opts.valueOf("server-host");
//        int serverPort = Integer.parseInt((String) opts.valueOf("server-port"));

    	String routerHost = "localhost";
        int routerPort = Integer.parseInt("3000");
        String serverHost = "localhost";
        int serverPort = Integer.parseInt("8007");
        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

        runClient(routerAddress, serverAddress);
    }
}

