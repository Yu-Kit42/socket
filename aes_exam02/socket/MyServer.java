package socket.aes_exam02.socket;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.aes_exam02.aes.AesClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

public class MyServer {
    private static final Logger log = LoggerFactory.getLogger(MyServer.class);
    // 암호화를 위한 변수들
    AesClass aes = null;
    String key = null;
    byte[] iv = null;

    // 입출력을 위한 Stream 객체들
    private InputStreamReader in = null;
    private InputStreamReader sr = null;
    private OutputStreamWriter out = null;

    // 서버 객체들
    private ServerSocket serSocket = null;
    private Socket socket = null;

    private SecureRandom srn = null;

    char[] byteArr = null;

    public static void main(String[] args) {
        MyServer myServer = new MyServer();

        // 초기화
        log.debug("프로그램 실행");
        myServer.initSocket();
        myServer.initInOut();
        myServer.initSecretKey();

        // 통신 시작
        log.info("클라이언트 연결");
        myServer.sendKey();
        while (myServer.reception())
            if (!myServer.send()) break;
        myServer.streamClose();
    }

    private void initSocket() {
        try {
            this.serSocket = new ServerSocket(8000);
            log.debug("서버 소켓 생성");
        } catch (IOException ignore) {
            log.debug("서버 소켓 생성 실패");
        }
        log.debug("클라이언트 소켓 대기중");
        try {
            this.socket = serSocket.accept();
            log.debug("소켓 생성");
        } catch (IOException ignore) {
            log.debug("소켓 생성 실패");
            ignore.printStackTrace();
        }
    }

    private void initInOut() {
        try {
            in = new InputStreamReader(socket.getInputStream());
            log.debug("수신 스트림 생성");
        } catch (IOException ignore) {
            log.error("수신 스트림 생성 실패");
        }

        sr = new InputStreamReader(System.in);
        log.debug("입력 스트림 생성");

        try {
            out = new OutputStreamWriter(socket.getOutputStream());
            log.debug("송신 스트림 생성");
        } catch (IOException ignore) {
            log.error("송신 스트림 생성 실패");
        }

    }

    private void initSecretKey() {
        try {
            if (srn == null) srn = SecureRandom.getInstanceStrong();
        } catch (Exception ignore) {
            log.error("랜덤 객체 생성 실패");
        }
        byte[] secretKeyByteArr = new byte[16];
        srn.nextBytes(secretKeyByteArr);
        log.debug("암호키 생성");
        key = Hex.encodeHexString(secretKeyByteArr);

        try {
            aes = new AesClass(secretKeyByteArr);
            log.debug("AES 암호키 객체 생성");
        } catch (Exception ignore) {
            log.error("AES 암호키 객체 생성 실패");
        }
    }

    private void sendKey() {
        createIv();
        String strIv = Hex.encodeHexString(iv);
        log.debug("암호키 송신");
        log.debug("암호키: {}\nIV: {}\n 방식: {}", key, strIv);
        try {
            out.write(key + ":" + strIv + ":");
            out.flush();
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
            return !inputMessage.equals("exit");
        } catch (IOException ignore) {
            log.error("수신 실패");
            return false;
        }
    }

    private boolean send() {
        createIv();
        byteArr = new char[512];
        try {
            sr.read(byteArr);
            String outputMessage = new String(byteArr).trim();
            out.write(Hex.encodeHexString(iv) + ":" + messageEncode(outputMessage));
            out.flush();
            log.debug("송신 보냄");
            return true;
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

    private void streamClose() {
        try {
            sr.close();
            socket.close();
            serSocket.close();
            in.close();
            out.close();
            log.debug("통신 종료");
        } catch (IOException ignored) {
            log.debug("통신 종료 절차 실패");
            System.exit(1);
        }
    }

}
