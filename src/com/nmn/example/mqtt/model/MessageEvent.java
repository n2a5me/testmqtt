package com.nmn.example.mqtt.model;

public class MessageEvent {
private String message;
private int group;
public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

public int getGroup() {
	return group;
}

public void setGroup(int group) {
	this.group = group;
}

}
