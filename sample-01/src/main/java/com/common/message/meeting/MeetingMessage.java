package com.common.message.meeting;

import com.common.message.BaseMessage;
import com.common.message.ResourceType;

import java.util.List;

public class MeetingMessage extends BaseMessage {

    private String siteUUID;
    private String meetingUUID;
    private String meetingTopic;
    private Long hostID;
    //private String unlistFlag;
    private boolean passwordProtected;
    private boolean requireLogin;
    private String scheduleType;
    private String serviceType;
    private String startTime;
    private long duration;
    private String rrule;
    private List<MeetingNotificationInvitee> invitees;

    // V2.0 Added
    private String meetingKey;
    private HostInfo hostInfo;
    private String creatorID;
    private String listType; // UNLIST | PRIVATE | PUBLIC | AUTHORIZED  // replace unlistFlag
    private String scheduleSource; // WEBEX_BOT | DEFAULT
    private boolean exceptionMeeting;
    private String seriesMeetingUUID;

    //V3.0 Added
    private String recurrenceType;//Repeat | NoRepeat | Exception
    private String occurrenceID;
    private String hostType;

    public MeetingMessage() {
        super();
    }

    @Override
    public String getKey() {
        return this.getSiteUUID() + "_" + this.getMeetingUUID();
    }

    @Override
    public String getResourceType() {
        return ResourceType.MEETING.getValue();
    }

    @Override
    public String getResourceID() {
        return this.getMeetingUUID();
    }

    public String getSiteUUID() {
        return siteUUID;
    }

    public void setSiteUUID(String siteUUID) {
        this.siteUUID = siteUUID;
    }

    public String getMeetingUUID() {
        return meetingUUID;
    }

    public void setMeetingUUID(String meetingUUID) {
        this.meetingUUID = meetingUUID;
    }

    public String getMeetingTopic() {
        return meetingTopic;
    }

    public void setMeetingTopic(String meetingTopic) {
        this.meetingTopic = meetingTopic;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }

    public boolean isRequireLogin() {
        return requireLogin;
    }

    public void setRequireLogin(boolean requireLogin) {
        this.requireLogin = requireLogin;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getRrule() {
        return rrule;
    }

    public void setRrule(String rrule) {
        this.rrule = rrule;
    }

    public List<MeetingNotificationInvitee> getInvitees() {
        return invitees;
    }

    public void setInvitees(List<MeetingNotificationInvitee> invitees) {
        this.invitees = invitees;
    }

    public Long getHostID() {
        return hostID;
    }

    public void setHostID(Long hostID) {
        this.hostID = hostID;
    }

    public String getMeetingKey() {
        return meetingKey;
    }

    public void setMeetingKey(String meetingKey) {
        this.meetingKey = meetingKey;
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public String getScheduleSource() {
        return scheduleSource;
    }

    public void setScheduleSource(String scheduleSource) {
        this.scheduleSource = scheduleSource;
    }

    public boolean isExceptionMeeting() {
        return exceptionMeeting;
    }

    public void setExceptionMeeting(boolean exceptionMeeting) {
        this.exceptionMeeting = exceptionMeeting;
    }

    public String getSeriesMeetingUUID() {
        return seriesMeetingUUID;
    }

    public void setSeriesMeetingUUID(String seriesMeetingUUID) {
        this.seriesMeetingUUID = seriesMeetingUUID;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public String getOccurrenceID() {
        return occurrenceID;
    }

    public void setOccurrenceID(String occurrenceID) {
        this.occurrenceID = occurrenceID;
    }

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }
}
