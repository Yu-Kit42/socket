package socket.bytesonarlint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class MyServer {
    private static final Logger log = LoggerFactory.getLogger(MyServer.class);

    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        ServerSocket serSocket = null;
        Socket socket = null;
        InputStreamReader sr = new InputStreamReader(System.in);

        // 랜덤값을 생성하기 위한 객체 생성
        Random rn = new Random();

        // 암호화를 위한 변수들
        AesClass aes = null;
        String key = null;
        String iv = null;

        // 처음 암호키를 전달하기 위함
        boolean isFirst = true;

        // 입출력을 위한 char 배열
        char[] byteArr = null;

        // 통신 유지를 위함
        boolean isConnection = true;


        // 프로그램 시작
        log.debug("프로그램 실행");
        MyServer myServer = new MyServer();

        // 소켓 객체 생성
        serSocket = myServer.createServerSocket();
        socket = myServer.createSocket(serSocket);

        // 입출력 객체 생성
        in = myServer.createInStream(socket);
        out = myServer.createOutStream(socket);


        // 암호화 코드 생성
        key = myServer.createKey(rn);
        iv = myServer.createKey(rn);
        aes = myServer.createAES(key, iv);

        // 통신
        while (isConnection) {
            // 맨 처음 통신할 경우 키를 넘겨줌
            if (isFirst) {
                assert out != null;
                myServer.sendKey(out, key, iv);
                isFirst = false;
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
            if (inputMessage.equals("exit") || outputMessage.equals("exit")) isConnection = false;
        }
        myServer.streamClose(sr, socket, serSocket, in, out);
    }

    private static String decode(String inputMessage, AesClass ase) {
        try {
            return ase.AesCBCDecode(inputMessage);
        } catch (Exception e) {
            log.error("복호화 실패");
            return "exit";
        }
    }

    private void streamClose(InputStreamReader sr, Socket socket, ServerSocket serSocket, InputStreamReader in, OutputStreamWriter out) {
        try {
            sr.close();
            if (socket != null) socket.close();
            if (serSocket != null) serSocket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            log.debug("통신 종료");
        } catch (IOException ignored) {
            log.debug("통신 종료 절차 실패");
            System.exit(1);
        }
    }

    private ServerSocket createServerSocket() {
        ServerSocket serSocket = null;
        try {
            serSocket = new ServerSocket(8000);
            log.debug("서버 소켓 생성");
        } catch (IOException e) {
            log.error("서버 소켓 생성 실패");
            e.printStackTrace();
        }
        return serSocket;
    }

    private Socket createSocket(ServerSocket serSocket) {
        Socket socket = null;
        if (serSocket != null) {
            try {
                socket = serSocket.accept();
                log.debug("소켓 생성");
            } catch (IOException e) {
                log.error("소켓 생성 실패");
            }
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

    // 16자리의 랜덤 숫자코드를 생성
    private String createKey(Random rn) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append(rn.nextInt(9) + 1);
        return sb.toString();
    }

    private AesClass createAES(String key, String iv) {
        AesClass aes = null;
        try {
            aes = new AesClass(key, iv);
        } catch (Exception e) {
            log.error("암호화 키 생성 실패");
        }
        return aes;
    }

    private void sendKey(OutputStreamWriter out, String key, String iv) {
        log.debug("암호키 생성");
        log.debug("key={}:iv={}", key, iv);
        try {
            out.write(key + ":" + iv);
            out.flush();
        } catch (IOException ignore) {
            log.error("암호키 생성 실패");
        }
    }

}
