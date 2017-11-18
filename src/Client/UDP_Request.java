package Client;

import java.net.MalformedURLException;
import java.net.URL;

import HttpMsg.RequestParameters;

public class UDP_Request {
	
	RequestParameters reqParams;
	boolean verbose;
	String inputFile;
	String outputFile;
	String url;
	
	
	public UDP_Request() {
		this.url = "";
		this.inputFile = "";
		verbose = false;
		reqParams = new RequestParameters();
	}
	public String getUrl() {
		return url;
	}

	public RequestParameters getRequestParameters(){
		return this.reqParams;
	}
	public void setUrl(String surl) {
		try {
			URL uurl = new URL(surl);
			this.url = surl;
			String path = uurl.getFile();
			this.reqParams.setPath(path);
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public String getInputFile() {
		return this.inputFile;
	}

	public void setInputFile(String file) {
		this.inputFile = file;
	}

	public boolean inOutput() {
		return !this.outputFile.equals("");
	}
}
