package Client;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Random;

import PacketLib.Packet;

public class PacketWrapper {
	
	private final static int PACKET_SIZE = 512;
	private final static int HEADER_SIZE = 118;
	private static final int WINDOW_SIZE = 8;
	
	String serverHost;
	int serverPort;
	InetSocketAddress serverAddress;
    
	UDPClientCmdParser inputParser = new UDPClientCmdParser();
	UDP_Request request;
	
	public Packet makePacket(String input) throws Exception {
		try {
			String[] data = input.split(" ");
			request = inputParser.parse(data);
			String serverHost = new URL(request.getUrl()).getHost();
			int serverPort = new URL(request.getUrl()).getPort();
	        serverAddress = new InetSocketAddress(serverHost, serverPort);
			
	        if(input.startsWith("post") && input.contains("-f")){
	        	
	        	byte[][] segmentedFile=segmentationForFileData(request.getInputFile());

	        	//send first window of packets 
	        	for(int i = 0; i < WINDOW_SIZE; i++){	
	        		
	        		if(i < segmentedFile.length){
	        			return  buildPacket(i, segmentedFile[i]);
	        		}
	        	 }
	        }
	        else if(input.startsWith("post") && input.contains("-d")){
	        	
	        }
	        else if(input.startsWith("get")){
	        	
	        }
		} catch (Exception e) {
			throw new Exception("ERROR Wrapping Packet");
		}
		return null;
		
	}
	
	private Packet buildPacket(int seq, byte[] message) {
        
		//Packet being Wrapped
		Packet packet = new Packet.Builder()
                 .setType(0)
                 .setAckN(new Random().nextInt(65000))
                 .setSeqN(new Random().nextInt(65000))
                 .setPortNumber(serverPort)
                 .setPeerAddress(serverAddress.getAddress())
                 .setPayload(message)
                 .create();
			return packet;
	}

	private static byte[][] segmentationForFileData(String filename) throws Exception{
        FileInputStream filestream = new FileInputStream(new File(filename));
        int size = (int)Math.ceil((double)(filestream.available())/(PACKET_SIZE - HEADER_SIZE));
        System.out.println("size is:"+ size);
        byte[][] segmentedFile = new byte[size][PACKET_SIZE - HEADER_SIZE];
        for(int i = 0; i < size; i++){
           for(int j = 0; j < (PACKET_SIZE - HEADER_SIZE); j++){
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
// "http://localhost:8085/Server/dataFile2.txt"