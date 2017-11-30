package selectiveR;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDirectory {
	ArrayList<DFile> files;
	String mainDir;
	FileDirectory(String path){
		files = new ArrayList<DFile>();
		this.mainDir = "";
		File dir = new File(this.mainDir).getAbsoluteFile();
		fileList(dir);
	}
	public void fileList(File dir){
		File[] subFiles = dir.listFiles();
		Pattern p1 = Pattern.compile(".class");
		Pattern p2 = Pattern.compile(".java");
		Pattern p3 = Pattern.compile(".jar");
//		Pattern p4 = Pattern.compile(".idx");
		Pattern p5 = Pattern.compile(".project");
		Pattern p4 = Pattern.compile(".git");
		
		for(File file :subFiles){
			if(file.isFile()){
				Matcher m1 = p1.matcher(file.getPath());
				Matcher m2 = p2.matcher(file.getPath()); 
				Matcher m3 = p3.matcher(file.getPath());
				Matcher m4 = p4.matcher(file.getPath());
				Matcher m5 = p5.matcher(file.getPath());
				DFile temp;
				if(m1.find()||m2.find()||m3.find()||m4.find()||m5.find())
					temp = new DFile(file.getAbsoluteFile(),true);
				else
					temp = new DFile(file.getAbsoluteFile(),false);
				files.add(temp);
			}else if(file.isDirectory()){
				fileList(file);
			}
		}
	}
	boolean fileExist(String path){
		for(DFile df:files){
			if(df.file.getPath().equals(path))
				return true;
		}
		return false;
	}
	
	boolean isAccessible(String path){
		for(DFile df:files){
			if(df.file.getPath().equals(path) && df.isAccessible())
				return true;
		}
		return false;
	}
	boolean isAccessible(File sfile){
		for(DFile df:files){
			if(df.file.compareTo(sfile)==0 && df.isAccessible())
				return true;
		}
		return false;
	}
	ArrayList<String> filesList(){
		ArrayList<String> res = new ArrayList<String>();
		for(DFile f:files){
			if(f.isAccessible())
				try{
					res.add(f.file.getCanonicalPath());
				}catch(Exception e){
					System.out.println("an exception has happend!");
				}
		}
		return res;
	}
	File mainDir(){
		File dir = new File(this.mainDir);
		return dir;
	}
}

//https://alvinalexander.com/blog/post/java/how-find-string-simple-regex-pattern-matcher