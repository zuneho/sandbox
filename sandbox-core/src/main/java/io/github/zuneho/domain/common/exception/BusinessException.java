package io.github.zuneho.domain.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus responseStatus;

    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.responseStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String errorMessage, HttpStatus responseStatus) {
        super(errorMessage);
        this.responseStatus = responseStatus;
    }

}
