package socket.aes_exam02.socket;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.aes_exam02.aes.AesClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

public class MyClient {
    private static final Logger log = LoggerFactory.getLogger(MyClient.class);
    // 암호화를 위한 변수들
    AesClass aes = null;
    String key = null;
    byte[] iv = null;
    // 입출력을 위한 char 배열
    char[] byteArr = null;

    // 입출력을 위한 Stream 객체들
    private InputStreamReader in = null;
    private InputStreamReader sr = null;
    private OutputStreamWriter out = null;

    private Socket socket = null;

    public static void main(String[] args) {
        MyClient myClient = new MyClient();

        // 초기화
        log.debug("프로그램 실행");
        myClient.initSocket();
        log.debug("클라이언트 생성 실행");
        myClient.initInOut();

        // 통신 시작
        myClient.receptionKey();
        while (myClient.send()) if (!myClient.reception()) break;
        myClient.streamClose();
    }


    private void initSocket() {
        try {
            this.socket = new Socket("10.51.10.175", 8000);
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

    private void receptionKey() {
        byteArr = new char[512];
        try {
            in.read(byteArr);
            String[] tp = new String(byteArr).trim().split(":");
            key = tp[0];
            iv = Hex.decodeHex(tp[1]);
            aes = new AesClass(Hex.decodeHex(key));
            log.info("수신 받은 암호키: {}", key);
        } catch (IOException | NoSuchAlgorithmException | DecoderException ignore) {
            log.error("암호키 수신 실패");
        }
    }

    private boolean send() {
        byteArr = new char[512];
        try {
            sr.read(byteArr);
            String outputMessage = new String(byteArr).trim();
            out.write(messageEncode(outputMessage));
            out.flush();
            log.debug("송신 보냄: {}", outputMessage);
            return !outputMessage.equals("exit");
        } catch (Exception e) {
            log.error("송신 실패");
            e.printStackTrace();
            return false;
        }
    }

    private boolean reception() {
        byteArr = new char[512];
        try {
            in.read(byteArr);
            String inputMessage = new String(byteArr).trim();
            log.info("수신 받은 암호 코드: {}", inputMessage);
            StringTokenizer st = new StringTokenizer(inputMessage, ":");
            iv = Hex.decodeHex(st.nextToken());
            inputMessage = st.nextToken();
            inputMessage = messageDecode(inputMessage);
            log.info("복호화 결과: {}", inputMessage);

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
            sr.close();
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
