package socket.jsonEx02;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

    private String header;

    private boolean sendKey;
    private int aesKeyLength;

    private boolean createIv;
    private String iv;

    private String msg;


    public Message(
            @JsonProperty("header")     String header,
            @JsonProperty("sendKey")    boolean sendKey,
            @JsonProperty("aesKeyLength")int aesKeyLength,
            @JsonProperty("createIv")   boolean createIv,
            @JsonProperty("iv")         String iv,
            @JsonProperty("msg")        String msg) {
        this.header = header;
        this.sendKey = sendKey;
        this.aesKeyLength = aesKeyLength;
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
