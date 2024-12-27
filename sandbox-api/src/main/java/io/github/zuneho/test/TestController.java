package io.github.zuneho.test;

import io.github.zuneho.domain.test.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TestController {
    private final TestService testService;

    @GetMapping("/test")
    public String test() {
        return testService.getTestMessage();
    }
}
