package com.common.message.meeting;

import java.util.ArrayList;
import java.util.List;

public class PostMeetingSharingUsers {
    private List<ShareUser> addedUsers = new ArrayList<>();
    private List<ShareUser> updatedUsers = new ArrayList<>();
    private List<ShareUser> removedUsers = new ArrayList<>();

    public List<ShareUser> getAddedUsers() {
        return addedUsers;
    }

    public void setAddedUsers(List<ShareUser> addedUsers) {
        this.addedUsers = addedUsers;
    }

    public List<ShareUser> getUpdatedUsers() {
        return updatedUsers;
    }

    public void setUpdatedUsers(List<ShareUser> updatedUsers) {
        this.updatedUsers = updatedUsers;
    }

    public List<ShareUser> getRemovedUsers() {
        return removedUsers;
    }

    public void setRemovedUsers(List<ShareUser> removedUsers) {
        this.removedUsers = removedUsers;
    }
}
