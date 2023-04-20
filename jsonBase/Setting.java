package socket.jsonBase;


public class Setting {

    //    @JsonProperty("sendKey")
    private boolean sendKey;

    private int aesKeyLength;

    private String secretKey;

    private boolean createIv;

    public Setting() {
        System.out.println("생성자 실행");
    }

    public boolean isSendKey() {
        return sendKey;
    }

    public int getAesKeyLength() {
        return aesKeyLength;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isCreateIv() {
        return createIv;
    }

    public Setting (
            boolean sendKey,
            int aesKeyLength,
            String secretKey,
            boolean createIv) {
        System.out.println("초기화 함수 실행");
        this.sendKey = sendKey;
        this.aesKeyLength = aesKeyLength;
        this.secretKey = secretKey;
        this.createIv = createIv;
    }
}
