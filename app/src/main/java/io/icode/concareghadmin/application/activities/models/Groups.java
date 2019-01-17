package io.icode.concareghadmin.application.activities.models;

public class Groups {

    private String GroupName;
    private String GroupIcon;

    public Groups() {
    }

    public Groups(String groupName, String groupIcon) {
        GroupName = groupName;
        GroupIcon = groupIcon;
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
}
