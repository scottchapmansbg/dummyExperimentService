package com.experiments.exceptionhandler;

public class NoExperimentsAvailableException extends RuntimeException {

    public NoExperimentsAvailableException(String message) {
        super(message);
    }

}
