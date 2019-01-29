package io.icode.concareghadmin.application.activities.notifications;

import io.icode.concareghadmin.application.activities.models.Groups;

public class SenderGroup {

    public DataGroup data;
    public String to;

    public SenderGroup(DataGroup data, String to) {
        this.data = data;
        this.to = to;
    }
}
