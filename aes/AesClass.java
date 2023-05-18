package socket.aes;


import org.apache.commons.codec.binary.Hex;
import socket.Config;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class AesClass {
    private final Cipher c;
    private SecretKeySpec secretKey;

    public AesClass() throws NoSuchPaddingException, NoSuchAlgorithmException {
        // Cipher 객체 인스턴스화(Java 에서는 PKCS#5으로 작성되지만 PKCS#7으로 실행된다.)
        c = Cipher.getInstance(Config.AES_PADDING);
    }

    public void setSecretKey(byte[] bytes) {
        this.secretKey = new SecretKeySpec(bytes, "AES");
    }

    public String aesCBCEncode(String plainText, byte[] iv) {
        if (checkArg(plainText, iv)) return "올바르지 않은 인자값";

        try {
            // Cipher 객체 초기화
            c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Encryption
            byte[] encryptByte = c.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Hex Encode
            return Hex.encodeHexString(encryptByte);

        } catch (Exception ignore) {
            return "암호화 실패";
        }
    }

    public String aesCBCDecode(String encodeText, byte[] iv) {
        if (checkArg(encodeText, iv)) return "올바르지 않은 인자값";
        try {
            // Cipher 객체 초기화
            c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Decode Hex
            byte[] decodeByte = Hex.decodeHex(encodeText);


            // Decryption
            return new String(c.doFinal(decodeByte), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "올바르지 않은 복호화\n" + e.getLocalizedMessage();
        }
    }

    private boolean checkArg(String text, byte[] iv) {
        return text.isEmpty() || iv.length != 16 || new String(iv).trim().isEmpty();
    }
}
