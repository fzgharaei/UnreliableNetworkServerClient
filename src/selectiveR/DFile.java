package selectiveR;

import java.io.File;

public class DFile {
	File file;
	boolean restricted;
	DFile(File file, boolean restricted){
		this.file = file;
		this.restricted = restricted;
	}
	
	boolean isAccessible(){
		return !restricted;
	}
	
}
