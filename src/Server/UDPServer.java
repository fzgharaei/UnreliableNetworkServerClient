package Server;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.TimerTask;

import PacketLib.Packet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
//https://github.com/michellefish/UDP-SelectiveRepeat/blob/master/UDP-SelectiveRepeat/src/UDPServer.java
public class UDPServer {
	final static int PACKET_SIZE = 512;
    final static int HEADER_SIZE = 118;
    final static int WINDOW_SIZE = 8;
    static int[] window;
    static int startWindow;
    static int numberOfTimeouts;
	private static void selectiveRepeat(String filename) throws Exception {
		
	}
	private static boolean allPacketsAcked(){
		return false;
	}
	private static byte[][] segmentation(String filename) throws Exception{
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
    private void listenAndServe(int port) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
//            logger.info("EchoServer is listening at {}", channel.getLocalAddress());
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
//                logger.info("Packet: {}", packet);
//                logger.info("Payload: {}", payload);
//                logger.info("Router: {}", router);
                System.out.println(payload);
                // Send the response to the router not the client.
                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.
                payload = "Hiii Client";
                Packet resp = packet.toBuilder()
                        .setPayload(payload.getBytes())
                        .create();
                channel.send(resp.toBuffer(), router);

            }
        }
    }
    private static class PacketTimeout extends TimerTask{
        private int seq;
        private byte[] message;
     	
        public PacketTimeout(int seq, byte[] message){
           this.seq = seq;
           this.message = message;
        }
     	
        public void run(){
           //if packet has not been ACKed
           if(window[seq] == 0){
              System.out.println("***PACKET TIMEOUT (seq: "+seq+")***");
              numberOfTimeouts++;
              try{
                 sendPacket(seq, message);
              }
                 catch (Exception e){}
           }
        }
    }
    public static void main(String[] args) throws IOException {
    	ServerOptionParser parser = new ServerOptionParser();
        

        parser.parse(args);
        int port = Integer.parseInt((String) parser.valueOf("port"));
        UDPServer server = new UDPServer();
        server.listenAndServe(port);
    }
}