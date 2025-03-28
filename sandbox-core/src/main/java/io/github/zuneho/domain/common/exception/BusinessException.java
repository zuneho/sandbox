package io.github.zuneho.domain.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HttpStatus responseStatus;

    public BusinessException(String errorMessage) {
        super(errorMessage);
        this.responseStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String errorMessage, HttpStatus responseStatus) {
        super(errorMessage);
        this.responseStatus = responseStatus;
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }
}
