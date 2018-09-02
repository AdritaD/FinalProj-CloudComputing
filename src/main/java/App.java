import java.util.Scanner;

import javax.crypto.spec.SecretKeySpec;

public class App {
	
	public static void main(String[] args) throws Exception{
		Show show = new Show();
		Download download = new Download();
		Upload upload = new Upload();
		Share_Download sd = new Share_Download();
		Share_Upload su = new Share_Upload();
		show.show();
    	boolean flag = true;
    	while(flag)
    	{
    		//Show options
    		System.out.println("Options:");
    	    System.out.println("[1] \t Upload File");
    	    System.out.println("[2] \t Download File");
    	    System.out.println("[3] \t Upload File for Share");
    	    System.out.println("[4] \t Download Share File");
    	    System.out.println("[5] \t Show Menu");
    	    System.out.println();
    	    
    		try
    		{
    			@SuppressWarnings("resource")
				Scanner reader = new Scanner(System.in);  // Reading from System.in
        		//prompt for option
        		System.out.println("Please Choose one of Option (Press any Other Key to Terminate.):");
        		int option = reader.nextInt(); // Scans the next token of the input as an int.
        		
        		switch(option)
        		{
        			case 1:	upload.upload(); break;
        			case 2: download.download(); break;
        			case 3: su.share_upload(); break;
        			case 4: sd.share_download(); break;
        			case 5: show.show(); break;
        			default: flag = false; System.out.println("Program Terminated."); break;
        		}
    		}
			
    		catch(Exception e)
    		{
    			flag = false;
    			System.out.println("Program Terminated.");
    		}
    	}
	}
}
