package com.nmn.example.mqtt.model;

public class User {
private String name;
private String message;
private String datetime;
private boolean sentEmo=false;
private int emoGroup=0;
private int emoResource;


public int getEmoGroup() {
	return emoGroup;
}
public void setEmoGroup(int emoGroup) {
	this.emoGroup = emoGroup;
}
public int getEmoResource() {
	return emoResource;
}
public void setEmoResource(int emoResource) {
	this.emoResource = emoResource;
}
public boolean isSentEmo() {
	return sentEmo;
}
public void setSentEmo(boolean sentEmo) {
	this.sentEmo = sentEmo;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getMessage() {
	return message;
}
public void setMessage(String message) {
	this.message = message;
}
public String getDatetime() {
	return datetime;
}
public void setDatetime(String datetime) {
	this.datetime = datetime;
}

}
