package io.icode.concareghadmin.application.activities.notifications;

import java.util.List;

public class TokenGroup {

    private List<String> token;

    public TokenGroup() {
    }

    public TokenGroup(List<String> token) {
        this.token = token;
    }

    public List<String> getToken() {
        return token;
    }

    public void setToken(List<String> token) {
        this.token = token;
    }
}
