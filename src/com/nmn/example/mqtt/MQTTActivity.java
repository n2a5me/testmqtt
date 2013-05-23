package com.nmn.example.mqtt;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nmn.example.mqtt.adapter.EmoticonsAdapter;
import com.nmn.example.mqtt.adapter.ListChatAdapter;
import com.nmn.example.mqtt.model.MessageEvent;
import com.nmn.example.mqtt.model.User;
import com.nmn.example.mqtt.utils.CommonUtil;

import de.greenrobot.event.EventBus;

public class MQTTActivity extends Activity implements OnClickListener{
	
	private final String TAG = "MQTTClient";
	private Timer timer;
	EditText destinationET = null;
	EditText messageET = null;
	private Button setTopic=null;
	TextView receiveEditText=null;
	Button sendButton = null;
	private ListView listChat;
	private ListChatAdapter adapter=null;
	private ProgressDialog progressDialog = null;
	private Handler handler;
	String sAddress = "tcp://mqtt.appota.com:1883";
	String sUserName = null;
	String sPassword = null;
	String sTOPIC = null;
	String sMessage = null;
	ArrayList<User> users=null;
	private String clientId;
	private MQTT mqtt = null;
	private GridView gridEmoticons;
	private FutureConnection connection = null;
	private Button more;
	private boolean boughtStickyDefault=true;
	private boolean backConfirm=false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handler=new Handler();
        users=new ArrayList<User>();
        mqtt = new MQTT();
        connection = mqtt.futureConnection();
        clientId = String.format("%-23.23s",System.getProperty("user.name") + "_" +
			      (UUID.randomUUID().toString())).trim().replace('-', '_');
        adapter=new ListChatAdapter(this, users, clientId);
        setupView();
        timer=new Timer();
        timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				checkMessages();
			}
		}, 1000,1000);
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	EventBus.getDefault().unregister(this);
    	disconnect();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	EventBus.getDefault().register(this);
//		if(connection!=null)
//		{
//			if(!connection.isConnected())
//			{
//				handler.post(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						connect();
//					}
//				});
//				
//			}
//		}else
//		{
//			connection = mqtt.futureConnection();
//			return;
//		}
//    	retryConnect();
    	Log.e("OnResume","On resume...");
    }
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	if(gridEmoticons.isShown())
    	{
    		more.performClick();
    		return;
    	}
    	if(!backConfirm)
    	{
    		backConfirm=true;
    		toast("Click back again to exit");
    		handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					backConfirm=false;
				}
			}, 3000);
    	}else
    	{
    		finish();
    	}
    }
    public void onEventMainThread(MessageEvent event){
		Log.e("onEventMainThread", "Here");
		sMessage=CommonUtil.convertMessage4Emoticon(clientId, event.getMessage(), event.getGroup());
		sendEmoticon();
//		handler.postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				more.performClick();
//			}
//		}, 500);
	}
	public void retryConnect() {
		mqtt = new MQTT();
		mqtt.setClientId(clientId);
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
//				sendButton.setEnabled(true);
				toast("Re-Connected!");
			}
			public void onFailure(Throwable e) {
				Log.e(TAG, "Problem connecting to host..Will retry in 10s. Exception connecting to " + sAddress + " - " + e);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						retryConnect();
					}
				}, 5000);
			}
		}));
	}
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	timer.cancel();
    	timer.purge();
    	timer=null;
    }
    public void setupView()
    {
    	// lock the screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	destinationET = (EditText)findViewById(R.id.destinationEditText);
    	messageET = (EditText)findViewById(R.id.messageEditText);
    	sendButton = (Button)findViewById(R.id.sendButton);
    	more=(Button)findViewById(R.id.more);
    	more.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(gridEmoticons.isShown())
				{
					gridEmoticons.setVisibility(View.GONE);
					more.setText("+");
				}else
				{
					gridEmoticons.setVisibility(View.VISIBLE);
					more.setText("-");
				}
			}
		});
    	sTOPIC = destinationET.getText().toString().trim();
    	sendButton.setOnClickListener(this);
    	gridEmoticons = (GridView) findViewById(R.id.gridEmoticons);
    	DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int _width = displaymetrics.widthPixels;
        gridEmoticons.setAdapter(new EmoticonsAdapter(this,(_width-30)/4));
    	setTopic=(Button)findViewById(R.id.setTopic);
    	setTopic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!TextUtils.isEmpty(destinationET.getText().toString()))
				{
					if(sTOPIC.equals(destinationET.getText().toString().trim()))
					{
						return;
					}
					connection.unsubscribe(new String[]{sTOPIC});
					sTOPIC=destinationET.getText().toString().trim();
					destinationET.setText(sTOPIC);
					Topic[] topics = {new Topic(sTOPIC, QoS.AT_LEAST_ONCE)};
					connection.subscribe(topics).then(onui(new Callback<byte[]>() {
						public void onSuccess(byte[] subscription) {
							toast("Joined the group!");
							renewListChat();
						}
						public void onFailure(Throwable e) {
							Log.e(TAG, "Exception sending message: " + e);
						}
					}));
				}else
				{
					toast("Empty topic");
				}
			}
		});
    	receiveEditText=(TextView)findViewById(R.id.receiveEditText);
    	listChat=(ListView)findViewById(R.id.listChat);
    	listChat.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(messageET.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(destinationET.getWindowToken(), 0);
				
			}

			
		});
    	listChat.setAdapter(adapter);
    	handler.postDelayed((new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(CommonUtil.isNetworkAvailable(MQTTActivity.this))
				{
					connect();
				}else
				{
					toast("No network available");
				}
				
			}
		}),1000);
    }
    private void renewListChat()
    {
    	users=new ArrayList<User>();
    	adapter=new ListChatAdapter(this, users, clientId);
    	listChat.setAdapter(adapter);
    }
	public void onClick(View v) {
		if(v == sendButton)
		{
			sTOPIC = destinationET.getText().toString().trim();
			sMessage = messageET.getText().toString().trim();
			
			// allow empty messages
			if(sTOPIC.equals(""))
			{
				toast("Input TOPIC name first.");
			}
			if(sMessage.equals(""))
			{
				toast("What's was that :-P");
			}else
			{
				send();
			}
		}
	}
	
	// callback used for Future
	<T> Callback<T> onui(final Callback<T> original) {
		return new Callback<T>() {
			public void onSuccess(final T value) {
				runOnUiThread(new Runnable(){
					public void run() {
						original.onSuccess(value);
					}
				});
			}
			public void onFailure(final Throwable error) {
				runOnUiThread(new Runnable(){
					public void run() {
						original.onFailure(error);
					}
				});
			}
		};
	}
	
	private void connect()
	{
		Log.e("Connect","Connecting...");
		mqtt.setClientId(clientId);
		sUserName="khanh";
		sPassword="khanh";
		try
		{
			mqtt.setHost(sAddress);
			Log.d(TAG, "Address set: " + sAddress);
		}
		catch(URISyntaxException urise)
		{
			Log.e(TAG, "URISyntaxException connecting to " + sAddress + " - " + urise);
		}
		
		if(sUserName != null && !sUserName.equals(""))
		{
			mqtt.setUserName(sUserName);
			Log.d(TAG, "UserName set: [" + sUserName + "]");
		}
		
		if(sPassword != null && !sPassword.equals(""))
		{
			mqtt.setPassword(sPassword);
			Log.d(TAG, "Password set: [" + sPassword + "]");
		}
		
		connection = mqtt.futureConnection();
		progressDialog = ProgressDialog.show(this, "", 
                "Connecting...", false);
		progressDialog.setCancelable(false);
		connection.connect().then(onui(new Callback<Void>(){
			public void onSuccess(Void value) {
				progressDialog.dismiss();
//				sendButton.setEnabled(true);
				toast("Connected");
			}
			public void onFailure(Throwable e) {
				toast("Problem connecting to host");
				Log.e(TAG, "Exception connecting to " + sAddress + " - " + e);
				progressDialog.dismiss();
			}
		}));

	}
	
	private void disconnect()
	{
		try
		{
			if(connection != null && connection.isConnected())
			{
				connection.disconnect().then(onui(new Callback<Void>(){
					public void onSuccess(Void value) {
//						sendButton.setEnabled(false);
						toast("Disconnected");
					}
					public void onFailure(Throwable e) {
						toast("Problem disconnecting");
						Log.e(TAG, "Exception disconnecting from " + sAddress + " - " + e);
					}
				}));
			}
			else
			{
				toast("Not Connected");
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, "Exception " + e);
		}
	}
	
	private void send()
	{
		if(!CommonUtil.isNetworkAvailable(this))
		{
			toast("No network available");
			return;
		}
		if(connection != null)
		{
			// automatically connect if no longer connected
			if(!connection.isConnected())
			{
				connect();
			}
			
				Topic[] topics = {new Topic(sTOPIC, QoS.AT_LEAST_ONCE)};
				connection.subscribe(topics).then(onui(new Callback<byte[]>() {
					public void onSuccess(byte[] subscription) {
						Log.d(TAG, "TOPIC: " + sTOPIC);
						Log.d(TAG, "Message: " + sMessage);
						// publish message
						String cMessage=CommonUtil.convertMessage(clientId, sMessage);
						connection.publish(sTOPIC, cMessage.getBytes(), QoS.AT_LEAST_ONCE, false);
//						TOPICET.setText("");
						messageET.setText("");
						toast("Message sent");
					}
					public void onFailure(Throwable e) {
						Log.e(TAG, "Exception sending message: " + e);
					}
				}));
			

		}
		else
		{
			toast("No connection has been made, please create the connection");
		}
	}
	private void sendEmoticon()
	{
		if(!CommonUtil.isNetworkAvailable(this))
		{
			toast("No network available");
			return;
		}
		if(connection != null)
		{
			// automatically connect if no longer connected
			if(!connection.isConnected())
			{
				connect();
			}
			
				Topic[] topics = {new Topic(sTOPIC, QoS.AT_LEAST_ONCE)};
				connection.subscribe(topics).then(onui(new Callback<byte[]>() {
					public void onSuccess(byte[] subscription) {
						Log.d(TAG, "TOPIC: " + sTOPIC);
						Log.d(TAG, "Message Emo: " + sMessage);
						// publish message
						connection.publish(sTOPIC, sMessage.getBytes(), QoS.AT_LEAST_ONCE, false);
//						TOPICET.setText("");
//						toast("Emoticon sent");
					}
					public void onFailure(Throwable e) {
						Log.e(TAG, "Exception sending message: " + e);
					}
				}));
			

		}
		else
		{
			toast("No connection has been made, please create the connection");
		}
	}
	
	public void checkMessages() {
		if(!CommonUtil.isNetworkAvailable(this))
		{
			return;
		}
//		Log.e("MQTT", "Start checking message..");
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
				User userItem=new User();
				userItem.setName(CommonUtil.extractMessage(messagePayLoad, false));
				userItem.setDatetime(CommonUtil.getCurrentDate());
				
				if(CommonUtil.isEmo(messagePayLoad))
				{
					userItem.setSentEmo(true);
					userItem.setEmoResource(Integer.parseInt(CommonUtil.extractEmoMessage(messagePayLoad, false)));
					userItem.setEmoGroup(Integer.parseInt(CommonUtil.extractEmoMessage(messagePayLoad, true)));
					userItem.setMessage(messagePayLoad);
				}else
				{
					userItem.setMessage(CommonUtil.extractMessage(messagePayLoad, true));
				}
				users.add(userItem);
				adapter.notifyDataSetChanged();
				listChat.setSelection(users.size()-1);
			}
			
			public void onFailure(Throwable e) {
				Log.w(TAG, "Exception receiving message: " + e);
			}
		}));
	}
	private void toast(String message)
	{
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}