package selectiveR;
   
   
   import java.io.*;
   import java.net.*;
   import java.util.*;

//https://github.com/michellefish/UDP-SelectiveRepeat/
   class UDPClient {
      static DatagramSocket clientSocket;
      final static int PORT = 10034;
      final static int PACKET_SIZE = 1024;
      final static int HEADER_SIZE = 118;
      final static int WINDOW_SIZE = 32;
      final static int ACK = 1;
      final static int NAK = 0;
      
      static Random generator = new Random(System.currentTimeMillis());
      static String ip;
      static double damageProb;
      static String filename;
      static String command;
      static int[] window;
      static int startWindow;
    
      static String reqUrl;
      public static void main(String args[]) throws Exception {
         //get user input and send GET request
         getUserInput();
         clientSocket = new DatagramSocket();
         URL url = new URL(ip);
         InetAddress serverIPAddress = InetAddress.getByName(url.getHost());
//         InetAddress serverIPAddress = InetAddress.getByName(ip);
         String request = sendRequestPacket(serverIPAddress, filename);
         System.out.println("\n-----TO SERVER-----\n" + request.trim());
         
         selectiveRepeat(serverIPAddress);
         clientSocket.close();
      }
      
      private static void selectiveRepeat(InetAddress serverIPAddress) throws Exception {
         int totalPacketsReceived = 0;
         int packetsDamaged = 0;
         
         ArrayList<byte[]> segmentedFile = new ArrayList<byte[]>();
         ArrayList<Integer>segmentedFileOrder = new ArrayList<Integer>();
         byte[] receiveData = new byte[PACKET_SIZE];
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         startWindow = 0;
         window = new int[WINDOW_SIZE];
         Arrays.fill(window, NAK);
      	
         while(true){
         	//receive packet
            clientSocket.receive(receivePacket);
            totalPacketsReceived++;
           
         	//check if last packet
            if(isNullPacket(receivePacket))
               break;		
            //gremlin then check if packet is corrupted
//            gremlin(receivePacket);
//            boolean isDamaged = isErrorDetected(receivePacket);
//            isDamaged = false;
            String s = new String(receivePacket.getData());
            System.out.println("\n-----FROM SERVER-----\n" + s.trim());
           
         	//send ACK
//            if(!isDamaged){
               int packetSeqNum = getSeqNum(receivePacket);
               String packetString = new String(receivePacket.getData());
               byte[] data = packetString.split("\r\n\r\n")[1].getBytes();
               segmentedFileOrder.add(packetSeqNum);
               segmentedFile.add(data);
               ackPacket(packetSeqNum);
               String ack = sendAcknowledgement(serverIPAddress);
               adjustWindow(packetSeqNum);
               System.out.println("\n-----TO SERVER-----\n" + ack.trim());
//            }
//            else{
//               System.out.println("***PACKET DAMAGED***");
//               packetsDamaged++;
//            }
         }
         reassembleFile(segmentedFile, segmentedFileOrder);
         
         System.out.println("\nTOTAL PACKETS RECEIVED: " +	totalPacketsReceived);
         System.out.println("PACKETS DAMAGED: " + packetsDamaged + "  " + (packetsDamaged/(double)totalPacketsReceived)*100 + "%");
      }
      
      private static void getUserInput() throws Exception {
         BufferedReader inputFromUser = new BufferedReader(new InputStreamReader(System.in));
//         System.out.println("router-host");
//         System.out.print("\nmachine to connect to: ");
//         ip = inputFromUser.readLine();
//         System.out.print("\nProbabilty that a given packet will be damaged: ");
//         damageProb = Double.parseDouble(inputFromUser.readLine());
         System.out.println("Enter command:");
//         System.out.print("\nEnter file name to request: ");
//         filename = inputFromUser.readLine();
         command = inputFromUser.readLine();
         cmdParser(command);
         inputFromUser.close();
      }
      
      private static String sendRequestPacket(InetAddress serverIPAddress, String filename) throws Exception {
         String request = ("GET " + filename + " HTTP/1.0\r\n");
         if(filename.equals("/")){
        	 byte[] requestData = new byte[request.length()];
             requestData = request.getBytes();
             DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverIPAddress, PORT);
             clientSocket.send(requestPacket);
             return request;
         }else{
         byte[] requestData = new byte[request.length()];
         requestData = request.getBytes();
         DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverIPAddress, PORT);
         clientSocket.send(requestPacket);
         return request;
         }
      }
      
      private static void ackPacket(int seqNum){
      	//ACK packet in window
         if(startWindow <= seqNum){
            if(seqNum-startWindow < WINDOW_SIZE)
               window[seqNum-startWindow] = ACK;
         }
      }
      
      private static void adjustWindow(int seqNum){
      	//shift window
         while(true){
            if(window[0] == ACK){
               for(int i = 0; i < WINDOW_SIZE-1; i++){
                  window[i] = window[i+1];
               }
               window[WINDOW_SIZE-1] = NAK;
               startWindow++;
            }
            else
               break;
         }
      }
      
      private static String sendAcknowledgement(InetAddress serverIPAddress) throws Exception {
         String ack = ("Seq: " + startWindow + "  Window: ");
         for(int i = 0; i < WINDOW_SIZE; i++){
            ack += window[i];
         }
         ack += "\r\n";
         byte[] ackData = new byte[ack.length()];
         ackData = ack.getBytes();
         DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, serverIPAddress, PORT);
         clientSocket.send(ackPacket);
         return ack;
      }
   	
      private static void reassembleFile(ArrayList<byte[]> segmentedFile, ArrayList<Integer> segmentedFileOrder) throws Exception {
         FileOutputStream filestream = new FileOutputStream(new File("new_" + filename));
         for(int i = 0; i < segmentedFileOrder.size(); i++){
            int index = segmentedFileOrder.indexOf(i);
            String msg = new String(segmentedFile.get(index));
            String data0 = msg.replaceAll("\00", "");
            String data = data0.replaceAll("\01", "");
            byte[] byteData = new byte[data.length()];
            byteData = data.getBytes();
            filestream.write(byteData);
         }
         filestream.close();
      }
      
//      private static void gremlin(DatagramPacket pkt){
//         int rand = generator.nextInt(10)+1;
//         if(rand <= damageProb*10){//corrupt packet
//            int change = generator.nextInt(10)+1;
//            if(change <= 5){//change 1 byte
//               int damage = generator.nextInt(pkt.getLength()-HEADER_SIZE)+HEADER_SIZE;
//               byte[] buf = pkt.getData(); 
//               if(buf[damage] == 0)
//                  buf[damage] += 1;
//               else
//                  buf[damage] -= 1;
//            }
//            else if(change <= 8){//change 2 bytes
//               for(int i = 0; i < 2; i++){
//                  int damage = generator.nextInt(pkt.getLength()-HEADER_SIZE)+HEADER_SIZE;
//                  byte[] buf = pkt.getData(); 
//                  if(buf[damage] == 0)
//                     buf[damage] += 1;
//                  else
//                     buf[damage] -= 1;
//               }
//            }
//            else{//change 3 bytes
//               for(int i = 0; i < 3; i++){
//                  int damage = generator.nextInt(pkt.getLength()-HEADER_SIZE)+HEADER_SIZE;
//                  byte[] buf = pkt.getData(); 
//                  if(buf[damage] == 0)
//                     buf[damage] += 1;
//                  else
//                     buf[damage] -= 1;
//               }
//            }
//         }
//      }
      
//      private static boolean isErrorDetected(DatagramPacket pkt){
//         //get checksum from packet
//         String packetString = new String(pkt.getData());
//         int index = packetString.indexOf("Checksum: ")+("Checksum: ".length());
//         int index2 = packetString.indexOf("\r\nSeq:");
//         int checksum = Integer.parseInt(packetString.substring(index, index2));
//      	
//      	//compute checksum
//         byte[] data = packetString.split("\r\n\r\n")[1].getBytes();
//         int computedChecksum = 0;
//         for(int i = 0; i < data.length; i++){
//            computedChecksum += (int)data[i];
//         }
//            
//      	//compare checksums
//         if(computedChecksum == checksum)
//            return false;
//         else
//            return true;
//      }
      
      private static int getSeqNum(DatagramPacket pkt){
         String packetString = new String(pkt.getData());
         int index = packetString.indexOf("Seq: ")+("Seq: ".length());
         int seq = Integer.parseInt(packetString.substring(index, index+3).trim());
         return seq;
      }
    
      private static boolean isNullPacket(DatagramPacket pkt){
         String pktData = new String(pkt.getData());
         byte[] dataByte = pktData.split("\r\n\r\n")[1].getBytes();
         if(dataByte[0] == 0)
            return true;
         else
            return false;
      }
      
      public static void cmdParser(String command) throws Exception{
    	  String[] args = command.split(" ");
    	  int i;
    	  reqUrl = args[args.length-1];
    	  ip = reqUrl;
    	  switch(args[0]){
    	  case "post":
				//request.get//requestParameters().setMethod("post");
				i=1;
				boolean dataSourseSet = false;
				while(i<args.length){
					switch(args[i]){
					
					
					 case "-d": 
					 case "--d":
						 if(args.length > i+1 && !dataSourseSet){
							 //request.get//requestParameters().setData(args[i+1]);
							 i+=2;
							 dataSourseSet = true;
						 }else
							 throw new Exception("BadSyntax");
						 break;
						 
					 case "-f":
					 case "--f":
						 if(args.length > i+1 && !dataSourseSet){
							 //request.setInputFile(args[i+1]);
							 dataSourseSet = true;
						 }else
							 throw new Exception("BadSyntax");
						 i+=2;
						 break;

					}
				}
				break;
				
			case "get":
				//request.get//requestParameters().setMethod("get");
				filename = args[1];
//			case "/": 
//				filename = "/";
				break;
//				
			default:
				throw new Exception("BadSyntax");
    	  }
    	  
//			return request;
      }
   }
