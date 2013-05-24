package com.nmn.example.mqtt;

import android.app.Application;

public class UIApplication extends Application {
	private String clientId=null;
	private String topicName=null;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

}
