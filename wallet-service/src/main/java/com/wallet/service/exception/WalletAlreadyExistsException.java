package com.wallet.service.exception;

public class WalletAlreadyExistsException extends RuntimeException {
    
    public WalletAlreadyExistsException(String message) {
        super(message);
    }
    
    public WalletAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

