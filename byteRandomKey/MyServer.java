package socket.byteRandomKey;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

public class MyServer {
    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        ServerSocket serSocket = null;
        Socket socket = null;
        InputStreamReader sr = new InputStreamReader(System.in);
        AesClass ase = null;
        boolean isFirst = true;
        char[] byteArr = null;

        // 서버 연결
        try {
            // 서버소켓 객체 생성
            serSocket = new ServerSocket(8000);

            // 연결 성공시 출력, 클라이언트 소켓 대기 
            System.out.println("[서버 실행]");
            socket = serSocket.accept();

            // 입출력 객체 생성
            in = new InputStreamReader(socket.getInputStream());
            out = new OutputStreamWriter(socket.getOutputStream());

            // 암호화 코드 생성
            String key = createKey();
            String iv = createKey();
            ase = new AesClass(key, iv);

            // 실제 통신
            while (true) {
                // 맨 처음 통신할 경우 키를 넘겨줌
                if (isFirst){
                    System.out.println("암호 키 발급");
                    System.out.println(key + ":" + iv);
                    out.write(key + ":" + iv);
                    out.flush();
                    isFirst = false;
                }

                // InputStreamReader 를 사용하여 바이트 단위로 받아옴
                byteArr = new char[512];
                in.read(byteArr);

                // 문자열로
                String inputMessage = new String(byteArr).trim();
                System.out.println("==== From Client ====\n" + inputMessage);

                // 복호화
                try {
                    inputMessage = ase.AesCBCDecode(inputMessage);
                } catch (Exception e){
                    System.out.println("복호화 실패");
                    out.write("exit");
                    out.flush();
                    break;
                }

                if ("exit".equals(inputMessage)) break; // 입력이 exit면 종료

                // 출력
                System.out.println("복호화 결과: " + inputMessage);
                System.out.print("회신하기: ");

                // char 배열로 입력을 받아오고 string 형으로 변환하여 출력
                byteArr = new char[512];
                sr.read(byteArr);
                String outputMessage = new String(byteArr).trim();
                out.write(byteArr);
                out.flush();
                if ("exit".equals(outputMessage)) break; // 출력이 exit면 종료
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { // 통신 종료시 버퍼, 연결 해제
            try {
                sr.close();
                socket.close();
                serSocket.close();
                System.out.println("연결 종료 되었습니다.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String createKey() {
        Random rn = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append(rn.nextInt(9) + 1);
        return sb.toString();
    }
}
