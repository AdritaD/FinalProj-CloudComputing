
import java.security.*;  
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class RSAEncryption {

	 public static String encryptByPublicKey(String data, RSAPublicKey publicKey)  
	            throws Exception {  
	        Cipher cipher = Cipher.getInstance("RSA");  
	        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
	        int key_len = publicKey.getModulus().bitLength() / 8;  
	        String[] datas = splitString(data, key_len - 11);  
	        String mi = "";  
	        for (String s : datas) {  
	            mi += bcd2Str(cipher.doFinal(s.getBytes()));  
	        }  
	        return mi;  
	    }  

	 public static String decryptByPrivateKey(String data, RSAPrivateKey privateKey)  
	            throws Exception {  
	        Cipher cipher = Cipher.getInstance("RSA");  
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);  
	        int key_len = privateKey.getModulus().bitLength() / 8;  
	        byte[] bytes = data.getBytes();  
	        byte[] bcd = ASCII_To_BCD(bytes, bytes.length);  
	        String ming = "";  
	        byte[][] arrays = splitArray(bcd, key_len);  
	        for(byte[] arr : arrays){  
	            ming += new String(cipher.doFinal(arr));  
	        }  
	        return ming;  
	    }  

	 
	 public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {  
	        byte[] bcd = new byte[asc_len / 2];  
	        int j = 0;  
	        for (int i = 0; i < (asc_len + 1) / 2; i++) {  
	            bcd[i] = asc_to_bcd(ascii[j++]);  
	            bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));  
	        }  
	        return bcd;  
	    }  
	  
	 public static byte asc_to_bcd(byte asc) {  
	        byte bcd;  
	  
	        if ((asc >= '0') && (asc <= '9'))  
	            bcd = (byte) (asc - '0');  
	        else if ((asc >= 'A') && (asc <= 'F'))  
	            bcd = (byte) (asc - 'A' + 10);  
	        else if ((asc >= 'a') && (asc <= 'f'))  
	            bcd = (byte) (asc - 'a' + 10);  
	        else  
	            bcd = (byte) (asc - 48);  
	        return bcd;  
	    }  

	 
	 public static String bcd2Str(byte[] bytes) {  
	        char temp[] = new char[bytes.length * 2], val;  
	  
	        for (int i = 0; i < bytes.length; i++) {  
	            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);  
	            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');  
	  
	            val = (char) (bytes[i] & 0x0f);  
	            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');  
	        }  
	        return new String(temp);  
	    }  
	 
	 
	 public static String[] splitString(String string, int len) {  
	        int x = string.length() / len;  
	        int y = string.length() % len;  
	        int z = 0;  
	        if (y != 0) {  
	            z = 1;  
	        }  
	        String[] strings = new String[x + z];  
	        String str = "";  
	        for (int i=0; i<x+z; i++) {  
	            if (i==x+z-1 && y!=0) {  
	                str = string.substring(i*len, i*len+y);  
	            }else{  
	                str = string.substring(i*len, i*len+len);  
	            }  
	            strings[i] = str;  
	        }  
	        return strings;  
	    }  


	 public static byte[][] splitArray(byte[] data,int len){  
	        int x = data.length / len;  
	        int y = data.length % len;  
	        int z = 0;  
	        if(y!=0){  
	            z = 1;  
	        }  
	        byte[][] arrays = new byte[x+z][];  
	        byte[] arr;  
	        for(int i=0; i<x+z; i++){  
	            arr = new byte[len];  
	            if(i==x+z-1 && y!=0){  
	                System.arraycopy(data, i*len, arr, 0, y);  
	            }else{  
	                System.arraycopy(data, i*len, arr, 0, len);  
	            }  
	            arrays[i] = arr;  
	        }  
	        return arrays;  
	    }  
	 
	 public String savePbKey(RSAPublicKey pbkey)
	 {
		String store_key = Base64.getEncoder().encodeToString(pbkey.getEncoded());
		return store_key;
	 }
	 
	 public String savePrKey(RSAPrivateKey prkey)
	 {
		String store_key = Base64.getEncoder().encodeToString(prkey.getEncoded());
		return store_key;
	 }
	 
	 public RSAPublicKey restorePbKey(String pbkey) throws InvalidKeySpecException, NoSuchAlgorithmException
	 {
		 byte[] decodedKey = Base64.getDecoder().decode(pbkey);
		 RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
		 return publicKey;
	 }
	 
	 public static RSAPrivateKey restorePRKey(String pbkey) throws InvalidKeySpecException, NoSuchAlgorithmException
	 {
		 byte[] decodedKey = Base64.getDecoder().decode(pbkey);
		 PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);  
		 KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
		 return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);  
	 }
}
