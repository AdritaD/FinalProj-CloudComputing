import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

public class Share_Download {
	
	//for download and decrypt share file
    public void share_download() throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, Exception
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
            
            //download file
            DbxDownloader<FileMetadata> downloader = client.files().download('/'+file_name);
            try {
                FileOutputStream out = new FileOutputStream(Constants.DEFAULT_PATH + file_name);
                downloader.download(out);
                out.close();
                
            } catch (DbxException ex) {
                System.out.println(ex.getMessage());
            }
        	
            // download key set
            DbxDownloader<FileMetadata> downloader_key = client.files().download("/key_for_"+file_name);
            try {
                FileOutputStream out_key = new FileOutputStream(Constants.DEFAULT_PATH + "/key_for_"+file_name);
                downloader_key.download(out_key);
                out_key.close();
                
            } catch (DbxException ex) {
                System.out.println(ex.getMessage());
            }
    	} catch (Exception ex) {
    		System.out.println("File Not Exist in Repository!");
    	}
        
    	
        try {
        	//get path for encryption file
            String path = Constants.DEFAULT_PATH + file_name;
            
            //get path for pr_key
            String path_private_key = Constants.PR_KEY;

            //get content in the file
            @SuppressWarnings("resource")
    		String file_content = new Scanner(new File(path)).useDelimiter("\\Z").next();
            
            String[] all_content = file_content.split("\\s+");
        	
            //file enc str
            String file_enc_str = all_content[0];
            
            //hamc str
            String hmac_str = all_content[1];
            
            
            //get path for key file
            String path_key = Constants.DEFAULT_PATH + "key_for_"+file_name;

            //get content in the file
            @SuppressWarnings("resource")
    		String key_content = new Scanner(new File(path_key)).useDelimiter("\\Z").next();
            
            
            //get private key
            @SuppressWarnings("resource")
    		String private_key_enc = new Scanner(new File(path_private_key)).useDelimiter("\\Z").next();
            //restore private key
            AESEncryption aes = new AESEncryption();
            RSAEncryption rsa = new RSAEncryption();
            SecretKeySpec mk = aes.getMK(pwd);
            String private_key = aes.decKey(private_key_enc, mk);
            
            //decrypt for keys
            RSAPrivateKey prk = rsa.restorePRKey(private_key);
            String keys_set = rsa.decryptByPrivateKey(key_content, prk);
            String[] keys_split = keys_set.split("\\s+");
            
            //key for file
            String file_key_str = keys_split[0];
            
            //iv
            String iv_str = keys_split[1];
            
            //hmac key
            String hmac_key_str = keys_split[2];
            
            //get keys
            SecretKey file_key = aes.restoreKey(file_key_str);
            byte[] iv = aes.stringToByte(iv_str);
            SecretKey hmac_key = aes.restoreKey(hmac_key_str);
            
            //decrypt
            if(aes.SHA512(file_enc_str, hmac_key).equals(hmac_str)) {
            	String dec = aes.doDec(file_enc_str, file_key, iv);
            	
            	//the path save download content
                String path_download = Constants.DOWNLOAD_PARH+file_name;
                		
                //write content into file
                Files.write(Paths.get(path_download), dec.getBytes());
                System.out.println("File has been Writen.");
                
                File temp = new File(path);
                File temp_0 = new File(path_key);
                if(temp.exists())
                {
                	temp.delete();
                }
                if(temp_0.exists())
                {
                	temp_0.delete();
                }
            }
            else
            {
            	System.out.println("File has been Damaged.");
            }
        	
        } catch (Exception e) {
        	System.out.println("Incorrect Master Key!");
        }
    }
}
