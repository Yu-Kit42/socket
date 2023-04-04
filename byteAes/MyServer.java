package socket.byteAes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class MyServer {
    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        ServerSocket serSocket = null;
        Socket socket = null;
        InputStreamReader sr = new InputStreamReader(System.in);
        AesClass ase;
        char[] byteArr;

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

            // 실제 통신
            while (true) {
                // InputStreamReader 를 사용하여 바이트 단위로 받아옴
                byteArr = new char[512];
                in.read(byteArr);

                // 문자열로
                String inputMessage = new String(byteArr).trim();
                System.out.println("==== From Client ====\n" + inputMessage);

                // 복호화
                try {
                    StringTokenizer st = new StringTokenizer(inputMessage, ":");
                    String code = st.nextToken();
                    ase = new AesClass(st.nextToken(), st.nextToken());
                    inputMessage = ase.AesCBCDecode(code);
                } catch (Exception e){
                    System.out.println("복호화 실패");
                }
                if ("exit".equals(inputMessage)) break; // 입력이 exit면 종료

                // 받아온 char배열 출력
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
}
