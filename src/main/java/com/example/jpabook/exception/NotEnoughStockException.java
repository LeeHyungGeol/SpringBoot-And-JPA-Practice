package com.example.jpabook.exception;

public class NotEnoughStockException extends RuntimeException {

    @Override
    public String getMessage() {
        return "재고가 부족합니다.";
    }

    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
}
