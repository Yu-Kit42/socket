package socket.final_exam;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.StringTokenizer;

public class MyClient {
    private static final Logger log = LoggerFactory.getLogger(MyClient.class);
    private final String ipAddress = "10.51.10.230";
    // 암호화
    private SecureRandom srn = null;
    private AesClass aes = null;
    private Socket socket = null;
    private String key = null;
    private byte[] iv = null;

    // 입출력
    private char[] byteArr = null;
    private InputStreamReader in = null;
    private InputStreamReader sr = null;
    private OutputStreamWriter out = null;
    private StringBuilder sb = null;

    // 설정
    private boolean isSendKey = true;
    private int aesKeyLength = 128;
    private boolean isCreateIv = true;

    public static void main(String[] args) {
        MyClient myClient = new MyClient();
        myClient.sr = new InputStreamReader(System.in);
        myClient.sb = new StringBuilder();
        while (myClient.runMenu())
            myClient.streamClose();
    }

    private boolean runMenu() {
        while (true) {
            printMenuMsg();
            byteArr = new char[512];
            try {
                sr.read(byteArr);
                switch (byteArr[0]) {
                    case '1':
                        if (!communicationExecute()) continue;
                        return true;
                    case '2':
                        isSendKey = !isSendKey;
                        setKey();
                        break;
                    case '3':
                        setAesKeyLength();
                        break;
                    case '4':
                        isCreateIv = !isCreateIv;
                        break;
                    case '9':
                        System.out.println("통신을 종료합니다");
                        return false;
                    default:
                        System.out.println("올바르지 않은 입력입니다");
                }
            } catch (Exception ignore) {
                log.error("메뉴에서 오류발생!");
                return false;
            }
        }
    }

    private boolean communicationExecute() throws DecoderException {
        if (aes == null) {
            try {
                aes = new AesClass();
            } catch (Exception ignore){
                log.error("aes 객체 생성 실패 {}", ignore.getLocalizedMessage());
            }
        }

        if (!initSocket()) return false;
        initStream();
        if (!sendSetting() || !receptionKey()) return false;
        aes.setSecretKey(Hex.decodeHex(key));
        while (send()) if (!reception()) break;

        return true;
    }

    private void setAesKeyLength() {
        try {
            byteArr = new char[512];
            System.out.println("1: 128, 2: 192, 3: 256 (범위 외 입력시 크기가 유지됩니다.)");
            sr.read(byteArr);
            if (byteArr[0] == '1') aesKeyLength = 128;
            else if (byteArr[0] == '2') aesKeyLength = 192;
            else if (byteArr[0] == '3') aesKeyLength = 256;
            else return;
            setKey();
        } catch (IOException ignore) {
            log.error("aes 키 길이 변경 실패");
        }
    }

    private void setKey() {
        if (isSendKey) return;
        try {
            if (srn == null) srn = SecureRandom.getInstanceStrong();
        } catch (Exception ignore) {
            log.error("랜덤 객체 생성 실패");
        }
        byte[] secretKeyByteArr = new byte[aesKeyLength / 8];
        srn.nextBytes(secretKeyByteArr);
        log.debug("암호키 생성");
        key = Hex.encodeHexString(secretKeyByteArr);

    }

    private boolean sendSetting() {

        sb.setLength(0);
        sb.append(isSendKey ? 1 : 0).append(":");
        sb.append(aesKeyLength).append(":");
        sb.append(isCreateIv ? 1 : 0).append(":");
        sb.append(isSendKey ? "" : key);
        String outMsg = sb.toString();
        log.info(outMsg);
        try {
            out.write(outMsg);
            out.flush();
            return true;
        } catch (IOException ignore) {
            log.error("설정 송신 실패");
            return false;
        }
    }

    private void printMenuMsg() {
        sb.setLength(0);
        sb.append("=====현재 설정=====").append("\n");
        sb.append("암호키 설정: ").append(isSendKey ? "서버에서 전송받음" : "클라이언트에서 전달: ").append(isSendKey ? "" : key).append("\n");
        sb.append("암호키 크기: AES").append(aesKeyLength).append("\n");
        sb.append("iv 생성 여부: ").append(isCreateIv ? "매번 생성" : "한번만 생성").append("\n");

        sb.append("1: 통신 시작").append("\n");
        sb.append("2: 암호키 전송 설정").append("\n");
        sb.append("3: 암호키 크기 설정").append("\n");
        sb.append("4: iv 생성 설정").append("\n");
        sb.append("9: 통신 종료");
        System.out.println(sb);
    }

    private boolean initSocket() {
        try {
            this.socket = new Socket(ipAddress, 8000);
            log.debug("소켓 생성");
            return true;
        } catch (IOException ignore) {
            log.debug("소켓 생성 실패");
            printError();
            return false;
        }
    }

    private void printError() {
        System.out.println("서버가 닫힌것 같습니다... 잠시후 시도해 주세요");
    }

    private void initStream() {
        try {
            in = new InputStreamReader(socket.getInputStream());
            out = new OutputStreamWriter(socket.getOutputStream());
            log.debug("송수신 스트림 생성");
        } catch (IOException ignore) {
            log.error("송수신 스트림 생성 실패");
        }
    }

    private boolean receptionKey() {
        byteArr = new char[512];
        try {
            System.out.println("서버 연결을 기다리는 중입니다...");
            in.read(byteArr);
            System.out.println("연결 성공");
            StringTokenizer st = new StringTokenizer(new String(byteArr).trim(), ":");
            if (isSendKey) {
                key = st.nextToken();
                log.info("수신 받은 암호키: {}", key);
            } else {
                log.info("클라이언트에서 암호키 전달: {}", key);
            }
            iv = Hex.decodeHex(st.nextToken());
            log.info("수신 받은 iv: {}", Hex.encodeHexString(iv));
            return true;
        } catch (IOException | DecoderException ignore) {
            log.error("암호키 수신 실패");
            printError();
            return false;
        }
    }


    private boolean send() {
        System.out.println(aes.toString());
        byteArr = new char[512];
        try {
            System.out.print("송신: ");
            sr.read(byteArr);
            String outputMessage = messageEncode(new String(byteArr).trim());
            out.write(outputMessage);
            out.flush();
            log.info("iv: {} key: {} text: {}", Hex.encodeHexString(iv), key, outputMessage);
            System.out.println("송신 보냄: " + outputMessage);
            return !outputMessage.equals("exit");
        } catch (Exception ignore) {
            log.error("송신 실패");
            return false;
        }
    }

    private boolean reception() {
        System.out.println(aes.toString());
        byteArr = new char[512];
        try {
            in.read(byteArr);
            String inputMessage = new String(byteArr).trim();
            log.info("수신 받은 암호 코드: {}", inputMessage);
            StringTokenizer st = new StringTokenizer(inputMessage, ":");
            if (isCreateIv) iv = Hex.decodeHex(st.nextToken());
            inputMessage = st.nextToken();
            inputMessage = messageDecode(inputMessage);
            System.out.println("복호화 결과: " + inputMessage);

            return !inputMessage.equals("exit");
        } catch (Exception ignore) {
            log.error("수신 실패");
            return false;
        }
    }

    private String messageEncode(String outputMessage) {
        return aes.aesCBCEncode(outputMessage, iv);
    }

    private String messageDecode(String inputMessage) {
        return aes.aesCBCDecode(inputMessage, iv);
    }

    private void streamClose() {
        try {
            if (socket == null) return;
            socket.close();
            in.close();
            out.close();
            log.debug("통신 종단");
        } catch (IOException ignored) {
            log.debug("통신 종단 절차 실패");
        }
    }

}
