package Server;

import java.util.HashMap;

public class ServerOptionParser {
	
	HashMap<String, String> serverKeywords;
	
	public ServerOptionParser() {

		serverKeywords = new HashMap<String,String>();
		//serverKeywords.put("port","8007");
		
	}
	
	void parse(String[] args){
		
		for(int i=0; i < args.length; i++){
			switch (args[i]) {
			
			case "port":
				serverKeywords.put("port","8007");
				i++;
				break;

			default:
				System.out.println("Bad input syntax");		
				}
		}
		
		
//		parser.acceptsAll(asList("port", "p"), "Listening port")
//        .withOptionalArg()
//        .defaultsTo("8007");
	}
	
	String valueOf(String str){
		// TO DO: make an actual value of
		return serverKeywords.get(str);
	}
}
