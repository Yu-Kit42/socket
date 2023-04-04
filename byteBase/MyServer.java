package socket.byteBase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        ServerSocket serSocket = null;
        Socket socket = null;
        InputStreamReader sr = new InputStreamReader(System.in);
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
                // 클라이언트에 입력을 대기, InputStreamReader를 사용하기 위해 char배열을 초기화
                System.out.println("클라이언트 입력 대기중");
                byteArr = new char[64];
                in.read(byteArr);
                String inputMessage = new String(byteArr).trim();
                if ("exit".equals(inputMessage)) break; // 입력이 exit면 종료

                // 받아온 char배열 출력
                System.out.println("From Client: " + inputMessage);
                System.out.print("회신하기: ");

                // char배열로 입력을 받아오고 string형으로 변환하여 출력
                byteArr = new char[64];
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
