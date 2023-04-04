package socket.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        BufferedReader in = null;
        PrintWriter out = null;
        ServerSocket serSocket = null;
        Socket socket = null;
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

        // 서버 연결
        try {
            // 서버소켓 객체 생성
            serSocket = new ServerSocket(8000);

            // 연결 성공시 출력, 클라이언트 소켓 대기 
            System.out.println("[서버 실행]");
            socket = serSocket.accept();

            // 입출력 객체 생성
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());

            // 실제 통신
            while (true) {
                // 클라이언트에 입력을 대기, BufferedReader의 인자값으로 소켓의 Stream을 넣어 문자열로 받아옴
                System.out.println("클라이언트 입력 대기중");
                String inputMessage = in.readLine();
                if ("exit".equals(inputMessage)) break; // 입력이 exit면 종료

                // 받아온 문자열 출력
                System.out.println("From Client: " + inputMessage);
                System.out.print("회신하기: ");

                // 문자열로 입력받고 문자열로 전달
                String outputMessage = bf.readLine();
                out.println(outputMessage);
                out.flush();
                if ("exit".equals(outputMessage)) break; // 출력이 exit면 종료
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { // 통신 종료시 버퍼, 연결 해제
            try {
                bf.close();
                socket.close();
                serSocket.close();
                System.out.println("연결 종료 되었습니다.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
