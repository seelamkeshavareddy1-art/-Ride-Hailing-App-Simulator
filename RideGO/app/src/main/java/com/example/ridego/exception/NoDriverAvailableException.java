
package com.example.ridego.exception;

public class NoDriverAvailableException extends Exception {

    public NoDriverAvailableException() {
        super("Sorry! No drivers are currently available.");
    }

    public NoDriverAvailableException(String message) {
        super(message);
    }
}
