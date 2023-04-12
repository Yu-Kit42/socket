package socket.final_exam.aes;


import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class AesClass {
    private static final Logger log = LoggerFactory.getLogger(AesClass.class);
    private Cipher c = null;
    private final SecretKeySpec secretKey;

    public AesClass(byte[] bytes) throws NoSuchAlgorithmException {
        this.secretKey = new SecretKeySpec(bytes, "AES");
    }


    public String aesCBCEncode(String plainText, byte[] iv) {
        byte[] encryptByte = null;
        try {
            // Cipher 객체 인스턴스화(Java에서는 PKCS#5, PKCS#7을 구별하지 않는다.)
            if (c == null) c = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Cipher 객체 초기화
            c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Encryption
            encryptByte = c.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("암호화 실패");
            e.printStackTrace();
            return "암호화 실패";
        }
        // Hex Encode
        assert encryptByte != null;
        return Hex.encodeHexString(encryptByte);
    }

    public String aesCBCDecode(String encodeText, byte[] iv) {
        byte[] decodeByte = null;
        try {
            // Cipher 객체 인스턴스화(Java에서는 PKCS#5 = PKCS#7이랑 동일)
            if (c == null) c = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Cipher 객체 초기화
            c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Decode Hex
            decodeByte = Hex.decodeHex(encodeText.toCharArray());

            // Decryption
            return new String(c.doFinal(decodeByte), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("복호화 실패");
            e.printStackTrace();
            return "올바르지 않은 복호화";
        }
    }
}
