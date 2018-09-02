import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.crypto.SecretKey;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.*;
import com.dropbox.core.v2.files.FileMetadata;

public class Download {
	//down load and decrypt file
    public void download() throws Exception
    {
    	boolean flag = false;
    	String pwd = "";
    	String file_name = "";
    	do
    	{
    		try
    		{
        		@SuppressWarnings("resource")
				Scanner reader = new Scanner(System.in);
    	    	//get pwd as master key
    	    	System.out.println("Please Enter your Password:");
    	    	pwd = reader.nextLine();
    	    	System.out.println("Please Enter File Name:");
    	    	file_name = reader.nextLine();
    	    	flag = false;
    		}
    		catch(Exception e)
    		{
    			System.out.println("Error input, please re-enter!");
    			flag = true;
    		}
		}while(flag);
    	
    	try {
    		// Create Dropbox client
        	DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
            DbxClientV2 client = new DbxClientV2(config, Constants.ACCESS_TOKEN);
            
            DbxDownloader<FileMetadata> downloader = client.files().download('/'+file_name);
            try {
                FileOutputStream out = new FileOutputStream(Constants.DEFAULT_PATH + file_name);
                downloader.download(out);
                out.close();
            } catch (DbxException ex) {
                System.out.println(ex.getMessage());
            }
            
            try{
            	//get path
                String path = Constants.DEFAULT_PATH + file_name;

                //get content in the file
                @SuppressWarnings("resource")
        		String file_content = new Scanner(new File(path)).useDelimiter("\\Z").next();
                
                String[] all_content = file_content.split("\\s+");
                
                //String of Secert key 
                String enc_file_str = all_content[0];
                String hmac_str = all_content[1];
                String key_sets_enc = all_content[2];
                
                AESEncryption aes = new AESEncryption();
                String key_sets = aes.decKey(key_sets_enc, aes.getMK(pwd));
                
                //get key from key sets
                String[] all_keys = key_sets.split("\\s+");
                
                //get file key
                SecretKey file_key = aes.restoreKey(all_keys[0]);
                
                //get iv
                byte[] iv = aes.stringToByte(all_keys[1]);
                
                //get hmac key
                SecretKey hmac_key = aes.restoreKey(all_keys[2]);
                
                //confirm integrity
                String hmac_copy = aes.SHA512(enc_file_str, hmac_key);

                
                if (aes.SHA512(enc_file_str, hmac_key).equals(hmac_str))
                {
                	//decrypt to plain text
                    String plain_text = aes.doDec(enc_file_str, file_key, iv); 
                    
                    //the path save download content
                    String path_download = Constants.DOWNLOAD_PARH+file_name;
                    		
                    //write content into file
                    Files.write(Paths.get(path_download), plain_text.getBytes());
                    System.out.println("File has been Writen.");
                    
                    File temp = new File(path);
                    if(temp.exists())
                    {
                    	temp.delete();
                    }
                    
                }
                else
                {
                	System.out.println("File has been Damaged");
                }
            } catch (Exception e) {
            	System.out.println("Incorrect Master Key!");
            }
    	} catch (Exception ex) {
    		System.out.println("File Not Exist in Repository!");
    	}    	
        
    }
}
