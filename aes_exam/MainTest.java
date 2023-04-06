package socket.aes_exam;

import org.apache.commons.codec.binary.Hex;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class MainTest {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        AesClass aes;
        SecureRandom srn = SecureRandom.getInstanceStrong();

        byte[] secretKey = new byte[16];
        srn.nextBytes(secretKey);

        byte[] iv = new byte[16];
        srn.nextBytes(iv);


        System.out.println(Hex.encodeHexString(secretKey) + " : " + Hex.encodeHexString(iv));
        aes = new AesClass(secretKey);
        String tp = aes.aesCBCEncode("테스트01", iv);
        System.out.println(tp);
        tp = aes.aesCBCDecode(tp, iv);
        System.out.println(tp);

    }
}
