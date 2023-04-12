package socket.final_exam.socket;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socket.final_exam.aes.AesClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.StringTokenizer;

public class MyClient {
    private static final Logger log = LoggerFactory.getLogger(MyClient.class);
    // 암호화를 위한 변수들
    private SecureRandom srn = null;
    private AesClass aes = null;
    private String key = null;
    private byte[] iv = null;

    // 입출력을 위한 char 배열
    private char[] byteArr = null;
    private StringBuilder sb = null;
    // 입출력을 위한 Stream 객체들
    private InputStreamReader in = null;
    private InputStreamReader sr = null;
    private OutputStreamWriter out = null;
    private Socket socket = null;
    private boolean isSendKey = true;
    private int aesKeyLength = 128;
    private boolean isCreateIv = true;
    private boolean isReConnection = false;


    public static void main(String[] args) {
        MyClient myClient = new MyClient();

        // 초기화
        log.debug("프로그램 실행");
        myClient.initSocket();
        log.debug("클라이언트 생성 실행");
        myClient.initInOut();

        // 통신 시작
        myClient.startMenu();
        myClient.streamClose();
    }

    private void startMenu() {
        try {
            while (true) {
                setMenuMsg();
                byteArr = new char[512];
                sr.read(byteArr);
                switch (byteArr[0]) {
                    case '1':
                        if (isReConnection) {
                            out.write("NotEnd");
                            out.flush();
                        }
                        sendSetting();
                        receptionKey();
                        while (send()) if (!reception()) break;
                        isReConnection = true;
                        break;
                    case '2':
                        isSendKey = !isSendKey;
                        initSecretKey();
                        break;
                    case '3':
                        initAesKeyLength();
                        break;
                    case '4':
                        isCreateIv = !isCreateIv;
                        break;
                    case '9':
                        System.out.println("통신을 종료합니다");
                        connectionEnd();
                        return;
                    default:
                        System.out.println("올바르지 않은 입력입니다");
                }
            }
        } catch (Exception ignore) {
            log.error("송신 실패");
            ignore.printStackTrace();
        }
    }

    private void initAesKeyLength() {
        try {
            byteArr = new char[512];
            System.out.println("1: 128, 2: 192, 3: 256");
            sr.read(byteArr);
            if (byteArr[0] == '1') aesKeyLength = 128;
            else if (byteArr[0] == '2') aesKeyLength = 192;
            else aesKeyLength = 256;
            initSecretKey();
        } catch (IOException ignore) {
            log.error("aes 키 길이 변경 실패");
        }
    }

    private void connectionEnd() {
        try {
            out.write("END");
            out.flush();
        } catch (IOException ignore) {
            log.error("연결 중단 실패");
        }
    }

    private void sendSetting() {
        sb.setLength(0);
        sb.append(isSendKey ? 1 : 0).append(":");
        sb.append(aesKeyLength).append(":");
        sb.append(isCreateIv ? 1 : 0).append(":");
        sb.append(isSendKey ? "" : key);
        log.info(sb.toString());
        try {
            out.write(sb.toString());
            out.flush();
        } catch (IOException ignore) {
            log.error("설정 송신 실패");
        }
    }

    private void setMenuMsg() {
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
        if (sb == null) sb = new StringBuilder();
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
            StringTokenizer st = new StringTokenizer(new String(byteArr).trim(), ":");
            if (isSendKey) {
                key = st.nextToken();
                log.info("수신 받은 암호키: {}", key);
            } else {
                log.info("클라에서 암호키 전달: {}", key);
            }
            iv = Hex.decodeHex(st.nextToken());
            log.info("수신 받은 iv: {}", Hex.encodeHexString(iv));
            aes = new AesClass(Hex.decodeHex(key));
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
            log.info("iv: {} key: {} text: {}", Hex.encodeHexString(iv), key, messageEncode(outputMessage));
            System.out.println("송신 보냄: " + outputMessage);
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
            if (isCreateIv) iv = Hex.decodeHex(st.nextToken());
            inputMessage = st.nextToken();
            inputMessage = messageDecode(inputMessage);
            System.out.println("복호화 결과: {}" + inputMessage);

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

    private void initSecretKey() {
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

        try {
            aes = new AesClass(secretKeyByteArr);
            log.debug("AES 암호키 객체 생성");
        } catch (Exception ignore) {
            log.error("AES 암호키 객체 생성 실패");
        }
    }
}
