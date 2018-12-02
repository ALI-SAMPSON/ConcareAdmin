package io.icode.concareghadmin.application.activities.Notifications;

public class Data {

    private String user;
    private int icon;
    private String title;
    private String body;
    private String sent;

    public Data() {
    }

    public Data(String user, int icon, String title, String body, String sent) {
        this.user = user;
        this.icon = icon;
        this.title = title;
        this.body = body;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}
