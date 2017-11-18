package Client;
import HttpMsg.ResponseParameters;

public class UDP_Response {
	

	ResponseParameters resParams;
	
	public UDP_Response(){
		this.resParams = new ResponseParameters();
	}
	
	public ResponseParameters getResponseParameters(){
		return this.resParams;
	}
}
