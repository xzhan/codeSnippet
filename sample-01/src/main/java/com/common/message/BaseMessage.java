package com.common.message;


import com.common.type.OperationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.UUID;

public abstract class BaseMessage {
	private static final String TRACKING_ID = "trackingID";
	private String version = "1.0";
	private String messageUUID; // The unique ID of current message instance.
	private String trackingID; // The tracking ID from the whole business context.
	private OperationType operationType; // The operation type of the event which triggers the current message.
	private String actionType; // When the operationType is beyond the provided "Create | Update | Delete", set
	// it to "Action", and then set the specific type in this field
	private long timestamp; // The time for current operation event (GMT)
	private String url; // The URL to retrieve detail data of current message object

	public BaseMessage() {
		this.messageUUID = UUID.randomUUID().toString();
		this.timestamp = System.currentTimeMillis();
		this.trackingID = (StringUtils.isEmpty(MDC.get(TRACKING_ID))
				? instanceTrackingID()
				: MDC.get(TRACKING_ID).toString());
	}

    /**
     * Ensure that the message with same unique key will be send to same partition to get ordered messages
     */
	@JsonIgnore
	public abstract String getKey();

	@JsonIgnore
	public abstract String getResourceType();

	@JsonIgnore
	public abstract String getResourceID();

	private String instanceTrackingID() {
		String trackingSessionID = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		trackingID = trackingSessionID + "_" + System.currentTimeMillis();
		return trackingSessionID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMessageUUID() {
		return messageUUID;
	}

	public void setMessageUUID(String messageUUID) {
		this.messageUUID = messageUUID;
	}

	public String getTrackingID() {
		return trackingID;
	}

	public void setTrackingID(String trackingID) {
		this.trackingID = trackingID;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
