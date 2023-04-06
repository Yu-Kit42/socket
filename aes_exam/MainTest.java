package socket.aes_exam;

import org.apache.commons.codec.binary.Hex;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MainTest {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        AesClass aes;
        SecureRandom srn = SecureRandom.getInstanceStrong();

        // 암호키를 랜덤으로 생성
        byte[] secretKey = new byte[32];
        srn.nextBytes(secretKey);

        // iv를 랜덤으로 생성
        byte[] iv = new byte[16];
        srn.nextBytes(iv);

        // 암호를 생성하는 과정에서 사용하는 secretKeySpec 은 byte 배열을 인자값으로 받기에 애초에 인자값으로 전달한다
        aes = new AesClass(secretKey);
        String tp = aes.aesCBCEncode("테스트01", iv); // iv 도 마찬가지로 바이트배열을 사용하기에 그냥 전달
        System.out.println(Hex.encodeHexString(secretKey) + " : " + Hex.encodeHexString(iv));

        // 복호화도 마찬가지
        System.out.println(tp);
        tp = aes.aesCBCDecode(tp, iv);
        System.out.println(tp);

    }
}
