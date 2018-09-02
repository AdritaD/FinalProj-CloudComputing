import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

public class Upload {
	
	//upload encrypted file
    public void upload() throws Exception
    {   
    	String path = "";
    	String pwd = "";
    	boolean flag = false;
    	do
    	{
    		try
    		{
    			//get directory
    	    	System.out.println("Please Enter file Directory:");
    	    	@SuppressWarnings("resource")
				Scanner reader = new Scanner(System.in);
    	    	path = reader.nextLine();
    	    	
    	    	//get pwd as master key
    	    	System.out.println("Please Enter your Password:");
    	    	pwd = reader.nextLine();
    	    	flag = false;
    		}
    		catch(Exception e)
    		{
    			System.out.println("Error input, please re-enter!");
    			flag = true;
    		}
    	}while(flag);
    	
    	// Create Dropbox client
        @SuppressWarnings("deprecation")
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, Constants.ACCESS_TOKEN);
       
        //get file name
        String[] split = path.split("\\\\");
        String file_name = split[split.length - 1];
        
        //get content in the file
        @SuppressWarnings("resource")
		String file_content = new Scanner(new File(path)).useDelimiter("\\Z").next();
        
        //initiallize instant for encryption
        AESEncryption aes = new AESEncryption();
        
        //get secret key for encrypt file
        SecretKey key_file = AESEncryption.generateKey();
        
        //get iv byte array
        byte[] iv_byte = aes.get_iv_bytes();
        
        
        //get key for hmac
        SecretKey key_hmac = AESEncryption.generateKey();
        
        //encrypt file content
        String file_enc = aes.doEnc(file_content, key_file, iv_byte);
        
        //hmac of cipher
        String file_hmac = aes.SHA512(file_enc, key_hmac);
        
        //Concatenate file key, iv and hmac key;
        String key_file_str = AESEncryption.saveKey(key_file);
        String key_hmac_str = AESEncryption.saveKey(key_hmac);
        String iv_str = aes.bytesToStr(iv_byte);
        String key_set = key_file_str + " " + iv_str + " " + key_hmac_str;
        
        SecretKeySpec mk = aes.getMK(pwd);
        
        //encrypt the key set with master key
        String enc_key_set = aes.encKey(key_set, mk);
        
        // All content in the file
        // string order splited by space
        // order in all content: file_enc -> file_hmac -> enc_key_set 
        // order in enc_key_set: file key -> iv -> hmac key 
        String all_content = file_enc + " " + file_hmac + " " + enc_key_set;
        
        //write all content in to temp file
        String temp_path = Constants.DEFAULT_PATH+file_name;
        
        //write content into file
        Files.write(Paths.get(temp_path), all_content.getBytes());   
        
        // Upload "test.txt" to Dropbox
        String metatdata = "/" + file_name;
        try{
        	InputStream in = new FileInputStream(temp_path);
            FileMetadata metadata = client.files().uploadBuilder(metatdata).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
            System.out.println(file_name + " Upload Success!");
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        File file = new File(temp_path);
        //delete temp file after upload
        if (file.exists())
        {
        	file.delete();
        }        
    }
}
