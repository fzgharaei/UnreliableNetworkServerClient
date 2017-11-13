package Client;

import java.util.HashMap;

public class ClientOptionParser {
	HashMap<String, String> keywords;
	public ClientOptionParser(){
		keywords = new HashMap<String,String>();
		keywords.put("router-host", "localhost");
		keywords.put("router-port", "3000");
		keywords.put("server-host", "localhost");
		keywords.put("server-port", "8007");
	}
	void parse(String[] args){
		for(int i =0;i<args.length;i++){
			switch(args[i]){
			case "router-host":
				keywords.put("router-host", args[i+1]);
			case "router-port":
			case "server-host":
			case "server-port": 
			default:
				System.out.println("Bad input syntax");
			}
		}
//		 parser.accepts("router-host", "Router hostname")
//         .withOptionalArg()
//         .defaultsTo("localhost");
//
// parser.accepts("router-port", "Router port number")
//         .withOptionalArg()
//         .defaultsTo("3000");
//
// parser.accepts("server-host", "EchoServer hostname")
//         .withOptionalArg()
//         .defaultsTo("localhost");
//
// parser.accepts("server-port", "EchoServer listening port")
//         .withOptionalArg()
//         .defaultsTo("8007");
	}
	
	String valueOf(String str){
		// TO DO: make an actual value of
		return str;
	}
}
