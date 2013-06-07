package com.nmn.example.mqtt;

import java.util.ArrayList;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.fusesource.mqtt.client.Topic;

import com.nmn.example.mqtt.model.ChatTopic;

import android.app.Application;
@ReportsCrashes(formKey = "dExlWVJNZGd1RW51MUt5WmtmUElrR1E6MQ")
public class UIApplication extends Application {
	private String clientId=null;
	private ArrayList<ChatTopic> topics=null;
	private static UIApplication instance;
	private Topic[] topicArr=null;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		ACRA.init(this);
		instance=this;
	}
	public static UIApplication getInstance()
	{
		return instance;
	}
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public ArrayList<ChatTopic> getTopics() {
		return topics;
	}
	public void setTopics(ArrayList<ChatTopic> topics) {
		this.topics = topics;
	}
	public Topic[] getTopicArr() {
		return topicArr;
	}
	public void setTopicArr(Topic[] topicArr) {
		this.topicArr = topicArr;
	}
	
}
