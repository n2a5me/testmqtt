package com.nmn.example.mqtt.events;

public class ReceivedMessageEvent {
public ReceivedMessageEvent(String mes,String tid)
{
	this.mes=mes;
	this.tid=tid;
}
private String mes;
private String tid;

public String getTid() {
	return tid;
}
public void setTid(String tid) {
	this.tid = tid;
}
public String getMes() {
	return mes;
}
public void setMes(String mes) {
	this.mes = mes;
}

}
