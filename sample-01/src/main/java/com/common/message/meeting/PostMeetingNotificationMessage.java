package com.common.message.meeting;


import com.common.message.BaseMessage;
import com.common.message.ResourceType;

import java.util.ArrayList;
import java.util.List;

public class PostMeetingNotificationMessage extends BaseMessage {
    private String siteUUID;
    private String ciOrgID;
    private String meetingInstanceID;
    private String meetingTopic;
    private String serviceType;

    private String realStartTime;
    private String realEndTime;
    private HostInfo hostInfo;
    private PostMeetingSharingUsers sharingList;
    private List<ParticipantInfo> participants = new ArrayList<>();
    private String version = "2.0";

    public String getSiteUUID() {
        return siteUUID;
    }

    public void setSiteUUID(String siteUUID) {
        this.siteUUID = siteUUID;
    }

    public String getMeetingInstanceID() {
        return meetingInstanceID;
    }

    public void setMeetingInstanceID(String meetingInstanceID) {
        this.meetingInstanceID = meetingInstanceID;
    }

    public String getMeetingTopic() {
        return meetingTopic;
    }

    public void setMeetingTopic(String meetingTopic) {
        this.meetingTopic = meetingTopic;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(String realStartTime) {
        this.realStartTime = realStartTime;
    }

    public String getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(String realEndTime) {
        this.realEndTime = realEndTime;
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    public PostMeetingSharingUsers getSharingList() {
        return sharingList;
    }

    public void setSharingList(PostMeetingSharingUsers sharingList) {
        this.sharingList = sharingList;
    }

    public List<ParticipantInfo> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantInfo> participants) {
        this.participants = participants;
    }

    public String getCiOrgID() {
        return ciOrgID;
    }

    public void setCiOrgID(String ciOrgID) {
        this.ciOrgID = ciOrgID;
    }

    @Override
    public String getKey() {
        return this.getSiteUUID() + "_" + this.getMeetingInstanceID();
    }

    @Override
    public String getResourceType() {
        return ResourceType.MEETING_INSTANCE.getValue();
    }

    @Override
    public String getResourceID() {
        return this.getMeetingInstanceID();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }
}
