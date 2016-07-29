package com.example.danie.schoolcashless.model.exception;

/**
 * A failed authentication due to an unknown server response
 */
public class BadResponseException extends Throwable {
    public final int status;

    public BadResponseException(int status) {
        this.status = status;
    }
}
