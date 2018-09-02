import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

/*
 * To Encrypt content (String)
 * 1.generate key --- SecretKey generateKey()
 * 2.generate iv --- IvParameterSpec get_iv()
 * 3.generate cipher --- String doEnc(String strToEncrypt, SecretKey secretKey, IvParameterSpec iv)
 * 
 * To export key into String --- String saveKey(SecretKey sk)
 * 
 * To import String type key --- SecretKey restoreKey(String store_key)
 * 
 * To do hmac(SHA512)
 * 1.generate key --- SecretKey generateKey()
 * 2.do hmac --- String SHA512 (String data, SecretKey secretKey)
 * 
 */


public class AESEncryption {
	
	//generate key for encryption
	//return the key for encryption
	public static SecretKey generateKey() throws NoSuchAlgorithmException, IOException
	{
	    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
	    keyGenerator.init(256); 
	    SecretKey key = keyGenerator.generateKey();
	    return key;
	}
	
	
	//convert to byte string for store in the file
	//return string that is able to restore to SecretKey
	public static String saveKey(SecretKey sk) throws UnsupportedEncodingException
	{
		String store_key = Base64.getEncoder().encodeToString(sk.getEncoded());
		return store_key;
	}
	
	
	//restore key
	//return secret key for encryption
	public static SecretKey restoreKey(String store_key) throws UnsupportedEncodingException
	{
		byte[] decodedKey = Base64.getDecoder().decode(store_key);
		SecretKey key_restore = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");	
		return key_restore;
	}
	
	//convert byte array to string
	public String bytesToStr(byte[] byt)
	{
		String str = Base64.getEncoder().encodeToString(byt);
		return str;
	}
	
	//convert string to byte array
	public byte[] stringToByte(String str)
	{
		byte[] iv = new byte[16];
		iv = Base64.getDecoder().decode(str);
		return iv;
	}
	
	//return iv byte[]
	public byte[] get_iv_bytes()
	{
		 int ivSize = 16;
	     byte[] iv = new byte[ivSize];
	     SecureRandom random = new SecureRandom();
	     random.nextBytes(iv);
	     return iv;
	}  
	
	//encrypt file with CBC mode
	public String doEnc(String strToEncrypt, SecretKey secretKey, byte[] iv) throws Exception
	{
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
	}
	
	
	//Decrypt the cipher
	public String doDec(String content, SecretKey secretKey, byte[] iv) throws Exception
	{
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(content));
        return new String(original);
	}
	
	
	// SHA 512 for HMAC
	public String SHA512 (String data, SecretKey secretKey) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException
	{
		Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(secretKey);
        byte[] macData = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(macData);
	}
	
	
	//get master key
	public SecretKeySpec getMK(String mk) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		byte[] key = mk.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16); // use only first 128 bit
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		return secretKeySpec;
	}
	
	
	//AES CBC without iv encryption
	public String encKey(String strToEncrypt, SecretKeySpec secretKey) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException
	{
		//SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
	}
	
	
	//AES CBC without iv decryption
	public String decKey(String content, SecretKeySpec secretKey) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		//SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));
        byte[] original = cipher.doFinal(Base64.getDecoder().decode(content));
        return new String(original);
	}
}