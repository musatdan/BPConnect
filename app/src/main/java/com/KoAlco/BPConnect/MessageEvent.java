package com.KoAlco.BPConnect;

public class MessageEvent {
    public final String message;
    public final int messageCode;

    public MessageEvent(String message, int messageCode)
    {
        this.message = message;
        this.messageCode = messageCode;
    }
}

