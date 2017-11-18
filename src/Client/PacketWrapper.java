package Client;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Random;

import PacketLib.Packet;

public class PacketWrapper {
	static UDPClientCmdParser inputParser;
	static UDP_Request request;
	
	public static Packet makePacket(String input) throws Exception {
		try {
			request = inputParser.parse(input.split(" "));
			String serverHost = new URL(request.getUrl()).getHost();
			int serverPort = new URL(request.getUrl()).getPort();
	        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
			
			Packet p = new Packet.Builder()
                    .setType(0)
                    .setAckN(new Random().nextInt(65000))
                    .setSeqN(new Random().nextInt(65000))
                    .setPortNumber(serverPort)
                    .setPeerAddress(serverAddress.getAddress())
                    .setPayload(request.getRequestParameters().getData().getBytes())
                    .create();
			return p;
			
		} catch (Exception e) {
			throw new Exception("ERROR Wrapping Packet");
		}
		
	}
}
