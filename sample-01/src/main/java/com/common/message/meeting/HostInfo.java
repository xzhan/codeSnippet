package com.common.message.meeting;

public class HostInfo {
    private String displayName;
    private String email;
    private String username;
    private String ciOrgID;
    private String ciUserUUID;
    private String userID;



    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getCiOrgID() {
        return ciOrgID;
    }

    public void setCiOrgID(String ciOrgID) {
        this.ciOrgID = ciOrgID;
    }

    public String getCiUserUUID() {
        return ciUserUUID;
    }

    public void setCiUserUUID(String ciUserUUID) {
        this.ciUserUUID = ciUserUUID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
