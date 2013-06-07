package com.nmn.example.mqtt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.nmn.example.mqtt.events.CommonEvent;
import com.nmn.example.mqtt.events.ReceivedMessageEvent;
import com.nmn.example.mqtt.model.ChatTopic;
import com.nmn.example.mqtt.utils.CommonUtil;
import com.nmn.example.mqtt.utils.Constants;

import de.greenrobot.event.EventBus;

public class MQTTService extends Service {
	private Timer timer;
	private MQTT mqtt = null;
	private FutureConnection connection = null;
	private String clientId=null;
	private String sPassword="khanh";
	private String sUserName="khanh";
	private String sAddress="tcp://mqtt.appota.com:1883";
	private Handler handler;
	private NotificationManager notiManager=null;
	private boolean isUINotActive=false;
	private int newMessages=0;
	private ArrayList<ChatTopic> topics;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("MQTTService","onCreate");
		mqtt = new MQTT();
        connection = mqtt.futureConnection();
        timer=new Timer();
        handler=new Handler();
        EventBus.getDefault().register(this);
        connect();
        timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				checkMessages();
			}
		}, 5000,300);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
//		super.onStart(intent, startId);
		
		if(intent.getAction().equalsIgnoreCase(Constants.SERVICE_ACTIONs.NEWCREATE.toString()))
		{
			clientId=intent.getExtras().getString("client_id");
			UIApplication my= (UIApplication) getApplication();
			my.setClientId(clientId);
		}else if(intent.getAction().equalsIgnoreCase(Constants.SERVICE_ACTIONs.START_TOPIC.toString()))
		{
			subcribeTopic();
		}else if(intent.getAction().equalsIgnoreCase(Constants.SERVICE_ACTIONs.RESET_COUNTER.toString()))
		{
			Log.e("RESET_COUNTER","");
			newMessages=0;
		}else if(intent.getAction().equalsIgnoreCase(Constants.SERVICE_ACTIONs.PING.toString()))
		{
			Log.e("PING","");
			ping();
		}
	}
	
	// callback used for Future
		<T> Callback<T> onui(final Callback<T> original) {
			return new Callback<T>() {
				public void onSuccess(final T value) {
					original.onSuccess(value);
				}
				public void onFailure(final Throwable error) {
					original.onFailure(error);
				}
			};
		}
	public void checkMessages() {
		if(!CommonUtil.isNetworkAvailable(this))
		{
			return;
		}
		
		try {
//			Log.e("MQTT-Service", "Start checking message..");
			connection.receive().then(onui(new Callback<Message>() {
				public void onSuccess(Message message) {
//							String receivedMesageTopic = message.getTopic();
					byte[] payload = message.getPayload();
					String messagePayLoad = new String(payload);
					message.ack();
//							connection.unsubscribe(new String[]{sTOPIC});
//					receiveEditText.setText(receiveEditText.getText().toString()+messagePayLoad);
					Log.e("CheckMessage","New message:"+messagePayLoad+". Topic:"+message.getTopic());
					if(!messagePayLoad.contains("~#@#~"))
					{
						return;
					}
					//write file
					String filePath=null;
						filePath=CommonUtil.getFilePath();
					String filename=message.getTopic()+".ca";
					
					Log.e("FilePath & fileName", filePath+"/"+filename);
					try {
						File codefile = new File(filePath);
						boolean isFolderAvailable = false;
						if(!codefile.exists())
						{
							isFolderAvailable=codefile.mkdirs();
						}else
						{
							isFolderAvailable=true;
						}
						if(!isFolderAvailable)
						{
							//alert & do nothing
							return;
						}
				        FileWriter writer = new FileWriter(codefile.getAbsoluteFile()+"/"+filename,true);
				        writer.append(messagePayLoad);
				        writer.append("\n");
				        writer.flush();
				        writer.close();
				        ReceivedMessageEvent mes=new ReceivedMessageEvent(messagePayLoad,message.getTopic());
				        if(isUINotActive)
				        {
				        	newMessages+=1;
				        	notiManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				        	Notification notification = new Notification(R.drawable.ic_launcher, 
									+ newMessages + " message(s)", System.currentTimeMillis());
							notification.flags = Notification.FLAG_AUTO_CANCEL;
							notification.defaults |= Notification.DEFAULT_SOUND;
					    	notification.defaults |= Notification.DEFAULT_VIBRATE;
							Intent notificationIntent = new Intent(MQTTService.this, ChatCenter.class);
							notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							PendingIntent pendingIntent = PendingIntent.getActivity(MQTTService.this, 0,
									notificationIntent, 0);
							notification.setLatestEventInfo(MQTTService.this, "Appota Chat",
									newMessages + " new message(s)",
									pendingIntent);
							notiManager.notify(2323, notification);
							Log.e("MQTTService", "Starting show noti. Num:"+newMessages);
				        }else
				        {
				        	EventBus.getDefault().post(mes);
				        }
				        
						}catch (IOException e) {
							Log.e("write file code", "File write failed: " + e.toString());
						} 
				}
				
				public void onFailure(Throwable e) {
					Log.w("checkMessages", "Exception receiving message: " + e);
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void connect()
	{
		if(!CommonUtil.isNetworkAvailable(this))
		{
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// reconnect since network isn't available
					Log.e("Re-connect","Re-connecting..");
					connect();
				}
			}, 5000);
			return;
		}
		Log.e("Connect","Connecting...");
		mqtt.setClientId(clientId);
		sUserName="khanh";
		sPassword="khanh";
		try
		{
			mqtt.setHost(sAddress);
		}
		catch(URISyntaxException urise)
		{}
		mqtt.setUserName(sUserName);
		mqtt.setPassword(sPassword);
		connection = mqtt.futureConnection(); 
		connection.connect().then(onui(new Callback<Void>(){
			public void onSuccess(Void value) {
				Log.e("Connect","onSuccess...");
				EventBus.getDefault().post(new CommonEvent.SuccessConnectEvent());
				
			}
			public void onFailure(Throwable e) {
				Log.e("Connect","onFailure...");
				EventBus.getDefault().post(new CommonEvent.FailConnectEvent());
			}
		}));

	}
	public void onEventMainThread(CommonEvent.UINotActiveEvent event){
		Log.e("UINotActiveEvent", "UINotActiveEvent");
		isUINotActive=true;
	}
	public void onEventMainThread(CommonEvent.UIActiveEvent event){
		Log.e("UIActiveEvent", "UIActiveEvent");
		isUINotActive=false;
		newMessages=0;
	}
	private void subcribeTopic()
	{
		if(!CommonUtil.isNetworkAvailable(this))
		{
			return;
		}
		Log.e("subcribeTopic","subcribeTopic");
		UIApplication myApp=UIApplication.getInstance();
		Topic[] topics = CommonUtil.getTopicArr(myApp.getTopics());
		connection.subscribe(topics).then(onui(new Callback<byte[]>() {
			public void onSuccess(byte[] subscription) {
				EventBus.getDefault().post(new CommonEvent.SuccessJoinTopicEvent());
			}
			public void onFailure(Throwable e) {
				EventBus.getDefault().post(new CommonEvent.FailJoinToicEvent());
			}
		}));
	}
	private void ping()
	{
		if(connection!=null && connection.isConnected())
		{
			Log.e("Ping","onPing...");
			EventBus.getDefault().post(new CommonEvent.PingSuccesEvent());
		}else
		{
			connection.connect().then(onui(new Callback<Void>(){
				public void onSuccess(Void value) {
					Log.e("Ping","onPing...");
					EventBus.getDefault().post(new CommonEvent.PingSuccesEvent());
					
				}
				public void onFailure(Throwable e) {
					Log.e("Ping","onFailure...");
					EventBus.getDefault().post(new CommonEvent.FailConnectEvent());
				}
			}));
		}
		
	}
}
