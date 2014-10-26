package com.dagrest.tracklocation.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import android.util.Base64;
import android.util.Log;

import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;

public class CryptoUtils {
	private static final String className = "com.dagrest.tracklocation.crypto.CryptoUtils";
	
	public static String encodeBase64(String toEncode) throws UnsupportedEncodingException{
		byte[] encodedByteArray;
	    encodedByteArray = Base64.encode(toEncode.getBytes("UTF-8"), Base64.DEFAULT);
	    String encodedStr = new String(encodedByteArray, "UTF-8");
	    
	    return encodedStr;
	}

	public static String decodeBase64(String toDecode) throws UnsupportedEncodingException{
	    byte[] decodedByteArray = Base64.decode(toDecode, Base64.DEFAULT);
	    String decodedStr = new String(decodedByteArray, "UTF-8");
	    
	    return decodedStr;
	}

	public static String hmacSha1(String value, String key) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();           
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes());

            // Convert raw bytes to Hex
            byte[] hexBytes = new Hex().encode(rawHmac);

            //  Covert array of Hex bytes to a String
            return new String(hexBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	public static String md5HexHash(String content){
		String methodName = "md5HexHash";
		String md5Hash = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] md5digest = md5.digest(content.getBytes());
			
			byte[] hexmd5Hash = new Hex().encode(md5digest);
			
			md5Hash = new String(hexmd5Hash, "UTF8");
		} catch (NoSuchAlgorithmException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
		}
		return md5Hash;
	}
}
