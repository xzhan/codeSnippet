package com.common.message.meeting;

public class ShareUser {
    private Long userID;
    private String role;

    private String ciOrgID;
    private String ciUserUUID;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
