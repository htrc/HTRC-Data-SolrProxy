package SolrDataAPI;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class RandomAccess {
	
	 public static void writeLog(String fileName, String content) {  
	        try {  
	          
	            RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");  
	           
	            long fileLength = randomFile.length();  
	            
	            randomFile.seek(fileLength);  
	            randomFile.writeBytes(content); 
	            randomFile.close();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	    }  
	  
	    public static void main(String[] args) {  
	        System.out.println("start");  
	        writeLog("E:/test.txt", "\n write to the end");  
	        System.out.println("end");  
	        Date today = new Date();
	        System.out.println(today.toString());
	    }  
	  

}
