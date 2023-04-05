package socket.logTest;

import org.slf4j.*;
public class LogTest {
    private static final Logger log = LoggerFactory.getLogger(LogTest.class);
    public static void main(String[] args) {
        log.info("로그 테스트1");
        log.debug("로그 테스트2");
        log.error("로그 테스트3 {} ", "ㅁㄴㅇㄴㅁㅇ");
    }
}
