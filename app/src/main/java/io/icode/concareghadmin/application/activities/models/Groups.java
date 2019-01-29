package io.icode.concareghadmin.application.activities.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Groups {

    private String GroupName;
    private String GroupIcon;
    private String GroupMessage;
    private List<String> GroupMembersIds;

    // unique to identify message to be deleted
    private String key;

    public Groups() {
    }

    public Groups(String groupName, String groupIcon,String groupMessage) {
        GroupName = groupName;
        GroupIcon = groupIcon;
        GroupMessage = groupMessage;
        GroupMembersIds = new ArrayList<>();
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupIcon() {
        return GroupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        GroupIcon = groupIcon;
    }

    public String getGroupMessage() {
        return GroupMessage;
    }

    public void setGroupMessage(String groupMessage) {
        GroupMessage = groupMessage;
    }

    public List<String> getGroupMembersIds() {
        return GroupMembersIds;
    }

    public void setGroupMembersIds(List<String> groupMembersIds) {
        GroupMembersIds = groupMembersIds;
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
