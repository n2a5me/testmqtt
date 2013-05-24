package com.nmn.example.mqtt.events;

public class ReceivedMessageEvent {
public ReceivedMessageEvent(String mes)
{
	this.mes=mes;
}
private String mes;
public String getMes() {
	return mes;
}
public void setMes(String mes) {
	this.mes = mes;
}

}
