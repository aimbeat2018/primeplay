package ott.primeplay.onepay;

import android.os.Build;

import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CommonFunctions {
    public static int generateRandomIntBetween(int min, int max) {
        Random random = new Random();
        int randomNumber = min + random.nextInt(max - min + 1);
        return randomNumber;
    }

    public static double generateRandomDoubleBetween(double min, double max) {

        // Generate a random number between min and max with two decimal places
        double randomDouble = (new Random().nextDouble() * (max - min) + min);
        randomDouble = Double.parseDouble(String.format("%.2f", randomDouble));
        return randomDouble;
    }

    public static String encryptText(String text) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Constants.secretKey.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(Constants.iv.getBytes());

        Cipher cipher = Cipher.getInstance(Constants.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(text.getBytes("UTF-8"));

        String ret = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ret = Base64.getEncoder().encodeToString(encryptedBytes);
        }

        return ret;
    }

    public static String decryptText(String encryptedText) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Constants.secretKey.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(Constants.iv.getBytes());

        Cipher cipher = Cipher.getInstance(Constants.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decodedBytes = new byte[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decodedBytes = Base64.getDecoder().decode(encryptedText);
        }

        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes, "UTF-8");
    }


}
