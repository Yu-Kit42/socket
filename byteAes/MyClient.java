package socket.byteAes;

import socket.byteAes.AesClass;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

// java security aes 128 암호화 -> key byte -> 출력 String, byte 전송
// key 128 랜덤 키를 생성 -> 서버로 byte -> 출력 -> String

public class MyClient {
    public static void main(String[] args) {
        // 입출력에 사용할 클래스를 나열
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        Socket socket = null;
        InputStreamReader sr = new InputStreamReader(System.in);
        AesClass ase;
        char[] byteArr;

        // 서버 연결
        try {
            // 소켓 객체 생성, 연결 성공 메시지
            socket = new Socket("10.51.10.88", 8000);
            System.out.println("서버에 연결성공 됐습니다");

            // 연결 성공시 출력, 클라이언트 소켓 대기 
            in = new InputStreamReader(socket.getInputStream());
            out = new OutputStreamWriter(socket.getOutputStream());

            // 해독키, CBC 키
            String key = "aesKey is 16Byte";
            String iv = "iv key is 16Byte";
            ase = new AesClass(key, iv);

            // 실제 통신
            while (true) {

                // 서버에 입력을 전송, 문자열로 전달
                System.out.print("전송하기: ");
                byteArr = new char[512];
                sr.read(byteArr);
                String outputMessage = new String(byteArr).trim();
                try {
//                    System.out.println(ase.AesCBCEncode(new String(byteArr)));
//                    out.write(byteArr);
//                    out.write("원래 값: " + new String(byteArr).trim());
//                    out.write("\n암호화: " + ase.AesCBCEncode(outputMessage));
//                    out.write("\n복호화 키: " + key + " : " + iv);
                    out.write(ase.AesCBCEncode(outputMessage) + ":" + key + ":" + iv);
                } catch (Exception e) {
                    System.out.println("암호화 실패!");
                    out.write("ERROR");
                }
                out.flush();
                if ("exit".equals(outputMessage)) break; // 출력이 exit면 종료

                // 입력을 문자열로 출력
                byteArr = new char[512];
                in.read(byteArr);
                String inputMessage = new String(byteArr).trim();
                System.out.println("From Server: " + inputMessage);
                if ("exit".equals(inputMessage)) break; // 입력이 exit면 종료
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally { // 마찬가지로 버퍼 해제, 연결 종료
            try {
                sr.close();
                socket.close();
                in.close();
                out.close();
                System.out.println("연결 종료 되었습니다.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
