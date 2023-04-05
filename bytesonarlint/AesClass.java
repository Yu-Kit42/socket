package socket.bytesonarlint;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AesClass {

    private SecretKeySpec secretKey;
    private IvParameterSpec IV;

    public AesClass(String reqSecretKey, String iv) {

        //바이트 배열로부터 SecretKey를 구축
        this.secretKey = new SecretKeySpec(reqSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
        this.IV = new IvParameterSpec(iv.getBytes());
    }

    //AES CBC PKCS5Padding 암호화(Hex | Base64)
    public String AesCBCEncode(String plainText) throws Exception {

        //Cipher 객체 인스턴스화(Java에서는 PKCS#5 = PKCS#7이랑 동일)
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

        //Cipher 객체 초기화
        c.init(Cipher.ENCRYPT_MODE, secretKey, IV);

        //Encrpytion/Decryption
        byte[] encrpytionByte = c.doFinal(plainText.getBytes("UTF-8"));

        //Hex Encode
        return Hex.encodeHexString(encrpytionByte);

    }

    //AES CBC PKCS5Padding 복호화(Hex | Base64)
    public String AesCBCDecode(String encodeText) throws Exception {

        //Cipher 객체 인스턴스화(Java에서는 PKCS#5 = PKCS#7이랑 동일)
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

        //Cipher 객체 초기화
        c.init(Cipher.DECRYPT_MODE, secretKey, IV);

        //Decode Hex
        byte[] decodeByte = Hex.decodeHex(encodeText.toCharArray());

        //Encrpytion/Decryption
        return new String(c.doFinal(decodeByte), "UTF-8");
    }

    /*
    public static void main(String[] args) throws Exception {

         // 키 값의 바이트 수에 따라서 달라집니다.
         // AES128 : 키값 16bytes
         // AES192 : 키값 24bytes
         // AES256 : 키값 32bytes

        String plainText = "AesPlainText";
        String key_128 = "aeskey1234567898";//AES-128는 128비트(16바이트)의 키
        String iv = "aesiv12345678912";

        AesClass ase_128_cbc = new AesClass(key_128, iv);
        String aes128CbcEncode = ase_128_cbc.AesCBCEncode(plainText);
        String aes128CbcDeocde = ase_128_cbc.AesCBCDecode(aes128CbcEncode);

        System.out.println("plainText: " + plainText);
        System.out.println();
        System.out.println("Aes128 Encode CBC: " + aes128CbcEncode);
        System.out.println("Aes128 Decode CBC: " + aes128CbcDeocde);

    }
*/
}