package io.icode.concareghadmin.application.activities.notifications;

import java.util.List;

public class DataGroup {

    private String user;
    private int icon;
    private String body;
    private String title;
    private List<String> sent;

    public DataGroup() {
    }

    public DataGroup(String user, int icon, String body, String title,  List<String> sent) {
        this.user = user;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sent = sent;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSent() {
        return sent;
    }

    public void setSent(List<String> sent) {
        this.sent = sent;
    }
}
