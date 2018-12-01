package io.icode.concareghadmin.application.activities.models;

public class Chats {

    private String sender;
    private String receiver;
    private String message;
    boolean isseen;

    public Chats(){}

    public Chats(String sender, String receiver, String message, boolean isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isseen;
    }

    public void setSeen(boolean isseen) {
        isseen = isseen;
    }
}
