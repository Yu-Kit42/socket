package socket.final_exam;


import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class AesClass {
    private static final String AES_PADDING = "AES/CBC/PKCS5Padding";
    private final SecretKeySpec secretKey;
    private final Cipher c;

    public AesClass(byte[] bytes) throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.secretKey = new SecretKeySpec(bytes, "AES");

        // Cipher 객체 인스턴스화(Java에서는 PKCS#5으로 작성되지만 PKCS#7으로 실행된다.)
        c = Cipher.getInstance(AES_PADDING);
    }

    public String aesCBCEncode(String plainText, byte[] iv) {
        byte[] encryptByte = null;
        try {
            // Cipher 객체 초기화
            c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Encryption
            encryptByte = c.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignore) {
            return "암호화 실패";
        }
        // Hex Encode
        return Hex.encodeHexString(encryptByte);
    }

    public String aesCBCDecode(String encodeText, byte[] iv) {
        try {
            // Cipher 객체 초기화
            c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Decode Hex
            byte[] decodeByte = Hex.decodeHex(encodeText);

            // Decryption
            return new String(c.doFinal(decodeByte), StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "올바르지 않은 복호화";
        }
    }
}
