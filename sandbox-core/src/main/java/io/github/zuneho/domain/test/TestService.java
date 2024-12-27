package io.github.zuneho.domain.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {

    public String getTestMessage() {
        log.info("test");
        return "Test";
    }
}
