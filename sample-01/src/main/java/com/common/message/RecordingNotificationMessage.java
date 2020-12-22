package com.common.message;


public class RecordingNotificationMessage extends BaseMessage {
    public enum CATEGORY {NBR, UPLOADED}
    public enum SERVICE_TYPE {MC, TC, EC, MISC}
    private String siteUUID;
    private String recordingUUID;
    private String recordingTopic;
    private CATEGORY category;
    private SERVICE_TYPE serviceType;
    private String ownerID;
    private OwnerInfo ownerInfo;
    private String createTime;
    private String metaType;
    private long duration;
    private long fileSize;
    private String meetingConfID;
    private String meetingUUID;
    private SharedReceiver sharedReceivers[];

    @Override
    public String getKey() {
        return this.getSiteUUID() + "_" + this.getRecordingUUID();
    }

    @Override
    public String getResourceType() {
        return ResourceType.RECORDING.getValue();
    }

    @Override
    public String getResourceID() {
        return this.getRecordingUUID();
    }

    public String getRecordingTopic() {
        return recordingTopic;
    }

    public void setRecordingTopic(String recordingTopic) {
        this.recordingTopic = recordingTopic;
    }

    public CATEGORY getCategory() {
        return category;
    }

    public void setCategory(CATEGORY category) {
        this.category = category;
    }

    public SERVICE_TYPE getServiceType() {
        return serviceType;
    }

    public void setServiceType(SERVICE_TYPE serviceType) {
        this.serviceType = serviceType;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }

    public void setOwnerInfo(OwnerInfo ownerInfo) {
        this.ownerInfo = ownerInfo;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMeetingConfID() {
        return meetingConfID;
    }

    public void setMeetingConfID(String meetingConfID) {
        this.meetingConfID = meetingConfID;
    }

    public SharedReceiver[] getSharedReceivers() {
        return sharedReceivers;
    }

    public void setSharedReceivers(SharedReceiver[] sharedReceivers) {
        this.sharedReceivers = sharedReceivers;
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

    public String getRecordingUUID() {
        return recordingUUID;
    }

    public void setRecordingUUID(String recordingUUID) {
        this.recordingUUID = recordingUUID;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getMetaType() {
        return metaType;
    }

    public void setMetaType(String metaType) {
        this.metaType = metaType;
    }

}
