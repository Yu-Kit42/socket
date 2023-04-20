package socket.jsonEx02;

import java.util.Map;

public class Message {

    private String header;

    private boolean sendKey;
    private int aesKeyLength;
    private String key;

    private boolean createIv;
    private String iv;

    private String msg;

    public Message(String header, boolean sendKey, int aesKeyLength, String key, boolean createIv, String iv, String msg) {
        this.header = header;
        this.sendKey = sendKey;
        this.aesKeyLength = aesKeyLength;
        this.key = key;
        this.createIv = createIv;
        this.iv = iv;
        this.msg = msg;
    }

    public String getHeader() {
        return header;
    }

    public boolean isSendKey() {
        return sendKey;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public String getKey() {
        return key;
    }

    public boolean isCreateIv() {
        return createIv;
    }

    public String getIv() {
        return iv;
    }

    public String getMsg() {
        return msg;
    }
}
