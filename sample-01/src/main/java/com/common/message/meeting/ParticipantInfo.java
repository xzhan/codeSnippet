package com.common.message.meeting;

public class ParticipantInfo {
    private String displayName;
    private String email;
    private Long userID;

    private String ciOrgID;
    private String ciUserUUID;

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

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
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
}
