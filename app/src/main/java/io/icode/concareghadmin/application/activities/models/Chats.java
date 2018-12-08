package io.icode.concareghadmin.application.activities.models;

import com.google.firebase.database.Exclude;

public class Chats {

    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;

    // unique to identify message to be deleted
    private String key;

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

    //getters and setters to store && retrieve the unique key of each message
    // excluding from database field
    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
