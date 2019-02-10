package io.icode.concareghadmin.application.activities.notifications;

import java.util.List;

import io.icode.concareghadmin.application.activities.models.Groups;

public class SenderGroup {

    public DataGroup data;
    public List<String> to;

    public SenderGroup(DataGroup data, List<String> to) {
        this.data = data;
        this.to = to;
    }
}
