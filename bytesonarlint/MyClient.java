package socket.bytesonarlint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class MyClient {
    private static final Logger log = LoggerFactory.getLogger(MyClient.class);

    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        Socket socket = null;
        InputStreamReader sr = new InputStreamReader(System.in);


        // 암호화를 위한 변수들
        AesClass aes = null;

        // 처음 암호키를 수신 받기 위함
        boolean isFirst = true;

        // 입출력을 위한 char 배열
        char[] byteArr = null;

        // 통신 유지를 위함
        boolean isConnection = true;


        // 프로그램 시작
        log.debug("프로그램 실행");
        MyClient myClient = new MyClient();

        // 소켓 객체 생성
        socket = myClient.createSocket();

        // 입출력 객체 생성
        in = myClient.createInStream(socket);
        out = myClient.createOutStream(socket);


        // 통신
        while (isConnection) {
            // 맨 처음 통신할 경우 키를 암호키를 받아옴
            if (isFirst) {
                aes = myClient.receptionKey(in, new char[512]);
                isFirst = false;
            }

            // 송신
            String outputMessage = "";
            byteArr = new char[512];
            try {
                sr.read(byteArr);
                outputMessage = new String(byteArr).trim();
                out.write(aes.AesCBCEncode(outputMessage));
                out.flush();
            } catch (Exception e) {
                log.error("송신 실패");
                e.printStackTrace();
            }

            // 수신
            byteArr = new char[512];
            try {
                in.read(byteArr);
            } catch (IOException e) {
                log.error("수신 실패");
                e.printStackTrace();
            }
            String inputMessage = new String(byteArr).trim();
            log.info("복호화 코드: {}", inputMessage);
            // 복호화
            inputMessage = decode(inputMessage, aes);
            log.info("복호화 결과: {}", inputMessage);

            if (inputMessage.equals("exit") || outputMessage.equals("exit")) isConnection = false;
        }
        myClient.streamClose(sr, socket, in, out);
    }

    private static String decode(String inputMessage, AesClass ase) {
        try {
            return ase.AesCBCDecode(inputMessage);
        } catch (Exception e) {
            log.error("복호화 실패");
            return "exit";
        }
    }

    private AesClass receptionKey(InputStreamReader in, char[] bytes) {
        AesClass aes = null;
        try {
            log.debug("암호화 키 받음");
            in.read(bytes);
            StringTokenizer st = new StringTokenizer(new String(bytes).trim(), ":");
            String key = st.nextToken();
            String iv = st.nextToken();
            log.debug("key={}:iv={}", key, iv);
            aes = new AesClass(key, iv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return aes;
    }

    private void streamClose(InputStreamReader sr, Socket socket, InputStreamReader in, OutputStreamWriter out) {
        try {
            sr.close();
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            log.debug("통신 종료");
        } catch (IOException ignored) {
            log.debug("통신 종료 절차 실패");
            System.exit(1);
        }
    }

    private Socket createSocket() {
        Socket socket = null;
        try {
            socket = new Socket("10.51.10.88", 8000);
            log.debug("소켓 생성");
        } catch (IOException e) {
            log.error("소켓 생성 실패");
        }
        return socket;
    }

    private InputStreamReader createInStream(Socket socket) {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(socket.getInputStream());
            log.info("입력 스트림 생성");
        } catch (IOException e) {
            log.error("입력 스트림 생성 실패");
        }
        return in;
    }

    private OutputStreamWriter createOutStream(Socket socket) {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(socket.getOutputStream());
            log.info("출력 스트림 생성");
        } catch (IOException e) {
            log.error("출력 스트림 생성 실패");
        }
        return out;
    }
}
