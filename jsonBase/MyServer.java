package socket.jsonBase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.aes.AesClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MyServer {
    private static final Logger log = LoggerFactory.getLogger(MyServer.class);

    // 암호화 
    AesClass aes = null;
    String key = null;
    String iv = null;
    char[] byteArr = null;
    private SecureRandom srn = null;

    // 입출력
    private InputStreamReader in = null;
    private InputStreamReader sr = null;
    private OutputStreamWriter out = null;
    private ObjectMapper objectMapper = null;

    // 설정
    private int aesKeyLength = 128;
    private boolean isSendKey = false;
    private boolean isCreateIv = true;


    // 서버 
    private ServerSocket serSocket = null;
    private Socket socket = null;

    public static void main(String[] args) {
        MyServer myServer = new MyServer();

        log.debug("프로그램 실행");
        myServer.initObject();

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
    }


    private void initObject() {
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
        } catch (Exception ignore) {
            log.error("aes 객체 생성 실패 {}", ignore.getLocalizedMessage());
        }
        objectMapper = new ObjectMapper();
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

            String settingMsg = new String(byteArr).trim();
            Setting setting = objectMapper.readValue(settingMsg, Setting.class);

            log.info("수신 받은 설정: {}", settingMsg);

            isSendKey = setting.isSendKey();
            aesKeyLength = setting.getAesKeyLength();
            isCreateIv = setting.isCreateIv();

            if (!isSendKey) {
                key = setting.getSecretKey();
                aes.setSecretKey(Hex.decodeHex(key));
                log.info("클라이언트에서 키 전달받음: {}", key);
            } else setSecretKey();

            return true;
        } catch (Exception ignore) {
            log.error("설정 수신 실패");
            ignore.printStackTrace();
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
        try {
            Msg msg = new Msg("sendKey", iv, key);
            String sendKeyJson = objectMapper.writeValueAsString(msg);

            out.write(sendKeyJson);
            out.flush();
            log.debug("암호키 송신");
            log.debug("암호키 json: {}", sendKeyJson);
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

            Msg msg = objectMapper.readValue(inputMessage, Msg.class);

            inputMessage = messageDecode(msg.getMessage());
            log.info("복호화 결과: {}", inputMessage);
            System.out.println("수신: " + inputMessage);
            return !inputMessage.equals("exit");
        } catch (Exception ignore) {
            log.error("수신 실패");
            ignore.printStackTrace();
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
            if (isCreateIv) createIv();

            result = outputMessage.equals("exit");
            outputMessage = messageEncode(outputMessage);

            Msg msg = new Msg("send", iv, outputMessage);
            outputMessage = objectMapper.writeValueAsString(msg);

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

    private String messageEncode(String outputMessage) throws DecoderException {
        return aes.aesCBCEncode(outputMessage, Hex.decodeHex(iv));
    }

    private String messageDecode(String inputMessage) throws DecoderException {
        return aes.aesCBCDecode(inputMessage, Hex.decodeHex(iv));
    }

    private void createIv() {
        byte[] byteTempArr = new byte[16];
        srn.nextBytes(byteTempArr);
        iv = Hex.encodeHexString(byteTempArr);
        log.debug("IV 생성: {}", iv);
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
