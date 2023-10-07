package com.boku.moneytransferservice.exception;

public class MissingParameterException extends Exception {
    public MissingParameterException(String parameterName) {
        super("Missing parameter: " + parameterName);
    }
}
