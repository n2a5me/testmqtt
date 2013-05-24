package com.nmn.example.mqtt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.nmn.example.mqtt.events.CommonEvent;
import com.nmn.example.mqtt.events.MessageEvent;
import com.nmn.example.mqtt.events.ReceivedMessageEvent;
import com.nmn.example.mqtt.model.User;
import com.nmn.example.mqtt.utils.CommonUtil;
import com.nmn.example.mqtt.utils.Constants;

import de.greenrobot.event.EventBus;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class MQTTService extends Service {
	private Timer timer;
	private MQTT mqtt = null;
	private FutureConnection connection = null;
	private String topicName=null;
	private String clientId=null;
	private String sPassword="khanh";
	private String sUserName="khanh";
	private String sAddress="tcp://mqtt.appota.com:1883";
	private Handler handler;
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
		}, 5000,1000);
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
			topicName=intent.getExtras().getString("topic_name");
			clientId=intent.getExtras().getString("client_id");
			UIApplication my= (UIApplication) getApplication();
			my.setClientId(clientId);
			my.setTopicName(topicName);
		}else if(intent.getAction().equalsIgnoreCase(Constants.SERVICE_ACTIONs.START_TOPIC.toString()))
		{
			topicName=intent.getExtras().getString("topic_name");
			Log.e("START_TOPIC",topicName);
			UIApplication my= (UIApplication) getApplication();
			my.setTopicName(topicName);
			subcribeTopic();
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
//		Log.e("MQTT-Service", "Start checking message..");
		connection.receive().then(onui(new Callback<Message>() {
			public void onSuccess(Message message) {
//						String receivedMesageTopic = message.getTopic();
				byte[] payload = message.getPayload();
				String messagePayLoad = new String(payload);
				message.ack();
//						connection.unsubscribe(new String[]{sTOPIC});
//				receiveEditText.setText(receiveEditText.getText().toString()+messagePayLoad);
				Log.e("CheckMessage","New message:"+messagePayLoad);
				if(!messagePayLoad.contains("~#@#~"))
				{
					return;
				}
				//write file
				String filePath=null;
					filePath=CommonUtil.getFilePath();
				String filename=topicName+".ca";
				
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
			        ReceivedMessageEvent mes=new ReceivedMessageEvent(messagePayLoad);
			        EventBus.getDefault().post(mes);
					}catch (IOException e) {
						Log.e("write file code", "File write failed: " + e.toString());
					} 
			}
			
			public void onFailure(Throwable e) {
				Log.w("checkMessages", "Exception receiving message: " + e);
			}
		}));
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
	}
	public void onEventMainThread(CommonEvent.UIActiveEvent event){
		Log.e("UIActiveEvent", "UIActiveEvent");
	}
	private void subcribeTopic()
	{
		if(!CommonUtil.isNetworkAvailable(this))
		{
			return;
		}
		Log.e("subcribeTopic","subcribeTopic");
		Topic[] topics = {new Topic(topicName, QoS.AT_LEAST_ONCE)};
		connection.subscribe(topics).then(onui(new Callback<byte[]>() {
			public void onSuccess(byte[] subscription) {
				EventBus.getDefault().post(new CommonEvent.SuccessJoinTopicEvent());
			}
			public void onFailure(Throwable e) {
				EventBus.getDefault().post(new CommonEvent.FailJoinToicEvent());
			}
		}));
	}
}
