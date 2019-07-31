package com.fiction.remotex;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

    public static String encrypt(String plainmsg, String plainkey) {
        try {
            byte[] data = plainmsg.getBytes(StandardCharsets.UTF_8);
            byte[] key = plainkey.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
            byte[] finalIvs = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            int len = (key.length<finalIvs.length)?key.length:finalIvs.length;
            System.arraycopy(key,0,finalIvs,0,len);
            IvParameterSpec ivps = new IvParameterSpec(finalIvs);
            SecretKeySpec secretKeySpec = new SecretKeySpec(finalIvs, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,ivps);
            byte[] tmp =  cipher.doFinal(data);
            return Base64.encodeToString(tmp,Base64.CRLF);
        } catch (Exception e) {
            Log.e("encryption error ",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String emsg, String plainkey) {
        try {
            byte[] data =  Base64.decode(emsg,Base64.CRLF) ;
            byte[] key = plainkey.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");

            byte[] finalIvs = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            int len = (key.length<finalIvs.length)?key.length:finalIvs.length;
            System.arraycopy(key,0,finalIvs,0,len);
            IvParameterSpec ivps = new IvParameterSpec(finalIvs);
            SecretKeySpec secretKeySpec = new SecretKeySpec(finalIvs, "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivps);
            return new String(cipher.doFinal(data),StandardCharsets.UTF_8) ;

        } catch (Exception e) {
            Log.e("decrypting error",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }



}
