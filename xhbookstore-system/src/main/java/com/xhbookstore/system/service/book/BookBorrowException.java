package com.xhbookstore.system.service.book;

public class BookBorrowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BookBorrowException(String message) {
        super(message);
    }
}
