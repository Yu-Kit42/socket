package socket.jsonBase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Msg {
    private String had;
    private String iv;
    private String message;

    public String getHad() {
        return had;
    }

    public String getIv() {
        return iv;
    }

    public String getMessage() {
        return message;
    }

    @JsonCreator
    public Msg(
            @JsonProperty("had") String had,
            @JsonProperty("iv") String iv,
            @JsonProperty("message") String message) {
        this.had = had;
        this.iv = iv;
        this.message = message;
    }
}
