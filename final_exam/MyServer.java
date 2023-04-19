package socket.final_exam;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.StringTokenizer;

public class MyServer {
    private static final Logger log = LoggerFactory.getLogger(MyServer.class);

    // 암호화 
    AesClass aes = null;
    String key = null;
    byte[] iv = null;
    char[] byteArr = null;
    private SecureRandom srn = null;

    // 설정 
    private int aesKeyLength = 128;
    private boolean isSendKey = false;
    private boolean isCreateIv = true;

    // 입출력 
    private InputStreamReader in = null;
    private InputStreamReader sr = null;
    private OutputStreamWriter out = null;

    // 서버 
    private ServerSocket serSocket = null;
    private Socket socket = null;

    public static void main(String[] args) {
        MyServer myServer = new MyServer();

        log.debug("프로그램 실행");
        myServer.initSerSocket();

        do {
            System.out.println("클라이언트 소켓 대기중");
            myServer.initSocket();
            System.out.println("클라이언트 소켓 연결");
            myServer.initStream();

            // 통신 전 환경 설정 체크
            if (!myServer.receptionSetting()) continue;
            myServer.sendKey();

            // 실제 통신
            while (myServer.reception()) if (myServer.send()) break;
            myServer.inPutStreamClose();

            // 통신을 계속할지 서버를 내릴지 선택
        } while (true);
//        myServer.closeServer();
    }

    /*
    private void closeServer() {
        try {
            sr.close();
            serSocket.close();
        } catch (IOException ignore) {
            log.error("서버 소켓 종료 실패");
        }
    }

    private boolean checkExitServer() {
        byteArr = new char[512];
        System.out.println("통신을 계속 하시겠습니까? Y/N");
        try {
            sr.read(byteArr);
            return byteArr[0] == 'N';
        } catch (Exception ignore) {
            log.error("종료 실패");
            return false;
        }
    }
    */

    private void initSerSocket() {
        try {
            this.serSocket = new ServerSocket(8000);
            log.debug("서버 소켓 생성");
        } catch (IOException ignore) {
            log.debug("서버 소켓 생성 실패");
        }
        sr = new InputStreamReader(System.in);
        log.debug("송신 입력 스트림 생성");

        try {
            srn = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException ignore) {
            log.debug("랜덤 객체 생성 실패");
        }
        try {
            aes = new AesClass();
        } catch (Exception ignore){
            log.error("aes 객체 생성 실패 {}", ignore.getLocalizedMessage());
        }
    }

    private void initSocket() {
        try {
            this.socket = serSocket.accept();
            log.debug("소켓 생성");
        } catch (IOException ignore) {
            log.debug("소켓 생성 실패");
        }
    }


    private void initStream() {
        log.debug("입출력 스트림 생성");
        try {
            in = new InputStreamReader(socket.getInputStream());
            out = new OutputStreamWriter(socket.getOutputStream());
            log.debug("통신 스트림 생성");
        } catch (IOException ignore) {
            log.error("통신 스트림 생성 실패");
        }

    }

    private boolean receptionSetting() {
        byteArr = new char[512];
        try {
            in.read(byteArr);
            String setting = new String(byteArr).trim();
            StringTokenizer st = new StringTokenizer(setting, ":");
            log.info("수신 받은 설정: {}", setting);

            isSendKey = st.nextToken().equals("0");
            aesKeyLength = Integer.parseInt(st.nextToken());
            isCreateIv = st.nextToken().equals("1");

            if (isSendKey) {
                key = st.nextToken();
                aes.setSecretKey(Hex.decodeHex(key));
                log.info("클라이언트에서 키 전달받음: {}", key);
            } else
                setSecretKey();

            return true;

        } catch (Exception ignore) {
            log.error("설정 수신 실패");
            return false;
        }
    }


    private void setSecretKey() {
        byte[] secretKeyByteArr = new byte[aesKeyLength / 8];
        srn.nextBytes(secretKeyByteArr);
        log.debug("암호키 생성");
        key = Hex.encodeHexString(secretKeyByteArr);

        try {
            aes.setSecretKey(secretKeyByteArr);
            log.debug("AES 암호키 객체 생성");
        } catch (Exception ignore) {
            log.error("AES 암호키 객체 생성 실패");
        }
    }

    private void sendKey() {
        createIv();
        String strIv = Hex.encodeHexString(iv);
        try {
            if (!isSendKey) {
                out.write(key + ":");
                log.info("클라이언트에 암호키 전달");
            }
            out.write(strIv);
            out.flush();
            log.debug("암호키 송신");
            log.debug("암호키: {} IV: {} 키 크기: {}", key, strIv, aesKeyLength);
        } catch (IOException ignore) {
            log.error("암호키 송신 실패");
        }
    }

    private boolean reception() {
        byteArr = new char[512];
        try {
            in.read(byteArr);
            String inputMessage = new String(byteArr).trim();
            log.info("수신 받은 암호 코드: {}", inputMessage);
            inputMessage = messageDecode(inputMessage);
            log.info("복호화 결과: {}", inputMessage);
            System.out.println(key);
            System.out.println("수신: " + inputMessage);
            return !inputMessage.equals("exit");
        } catch (IOException ignore) {
            log.error("수신 실패");
            return false;
        }
    }

    private boolean send() {
        byteArr = new char[512];
        boolean result;
        try {
            System.out.print("송신: ");
            sr.read(byteArr);
            String outputMessage = new String(byteArr).trim();
            if (isCreateIv) {
                createIv();
                out.write(Hex.encodeHexString(iv) + ":");
            }
            result = outputMessage.equals("exit");
            outputMessage = messageEncode(outputMessage);
            out.write(outputMessage);
            out.flush();
            log.info("암호문: {}", outputMessage);
            log.debug("송신 보냄");
            return result;
        } catch (Exception ignore) {
            log.error("송신 실패");
            return false;
        }
    }

    private String messageEncode(String outputMessage) {
        return aes.aesCBCEncode(outputMessage, iv);
    }

    private String messageDecode(String inputMessage) {
        return aes.aesCBCDecode(inputMessage, iv);
    }

    private void createIv() {
        byte[] byteTempArr = new byte[16];
        srn.nextBytes(byteTempArr);
        iv = byteTempArr;
        String tp = Hex.encodeHexString(iv);
        log.debug("IV 생성: {}", tp);
    }

    private void inPutStreamClose() {
        try {
            socket.close();
            in.close();
            out.close();
            log.debug("통신 종료");
        } catch (IOException ignored) {
            log.debug("통신 종료 절차 실패");
            System.exit(1);
        }
    }
}
