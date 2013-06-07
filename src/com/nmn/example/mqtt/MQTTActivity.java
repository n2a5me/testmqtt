package com.nmn.example.mqtt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nmn.example.mqtt.adapter.EmoticonsAdapter;
import com.nmn.example.mqtt.adapter.RoomChatAdapter;
import com.nmn.example.mqtt.events.CommonEvent;
import com.nmn.example.mqtt.events.MessageEvent;
import com.nmn.example.mqtt.events.ReceivedMessageEvent;
import com.nmn.example.mqtt.model.ChatTopic;
import com.nmn.example.mqtt.model.User;
import com.nmn.example.mqtt.utils.CommonUtil;
import com.nmn.example.mqtt.utils.Constants;

import de.greenrobot.event.EventBus;

public class MQTTActivity extends Activity implements OnClickListener{
	
	private final String TAG = "MQTTClient";
	private Timer timer;
	EditText destinationET = null;
	EditText messageET = null;
	private Button setTopic=null;
	Button sendButton = null;
	private ListView listChat;
	private RoomChatAdapter adapter=null;
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
	private boolean customTitleSupported;
	private boolean isLastMsgIcon=false;
	private int lastMsgIcon;
	private String lastMsg;
	private boolean sentNewMessage=false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("OnActivityCreate","On Create...");
        customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        if ( customTitleSupported ) {
        	Log.e("SupportCustom","true");
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_header);
            }else
            {
            	Log.e("SupportCustom","false");
            }
        handler=new Handler();
        users=new ArrayList<User>();
        mqtt = new MQTT();
        connection = mqtt.futureConnection();
        UIApplication myapp=(UIApplication)getApplication();
        if(myapp.getClientId()!=null)
        {
        	clientId=myapp.getClientId();
        }else
        {
        	clientId = String.format("%-23.23s",System.getProperty("user.name") + "_" +
			      (UUID.randomUUID().toString())).trim().replace('-', '_');
        	Log.e("getClientIdFromApplication","N/A");
        }
        adapter=new RoomChatAdapter(this, users, clientId);
        setupView();
        timer=new Timer();
        timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				connectToService(null, Constants.SERVICE_ACTIONs.PING.toString());
			}
		}, 3000,3000);
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
//    	retryConnect();
    	Log.e("OnActivityResume","On resume...");
    	
    }
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	if(gridEmoticons.isShown())
    	{
    		more.performClick();
    	}
    	super.onBackPressed();
    }
    public void onEventMainThread(MessageEvent event){
		Log.e("onEventMainThread", "Here");
		sMessage=CommonUtil.convertMessage4Emoticon(clientId, event.getMessage(), event.getGroup());
		try {
			sendEmoticon();
		} catch (Exception e) {
			toast("Unable to send message");
		}
	}
    public void onEventMainThread(CommonEvent.FailConnectEvent event)
    {
        headerFail();
    }

	private void headerFail() {
        ImageView myConnectIcon = (ImageView) findViewById(R.id.connectIcon);
        if(myConnectIcon!=null)
        {
        	if(customTitleSupported)
        	{
        		myConnectIcon.setImageResource(R.drawable.not_connected);
        	}
        	
        }
	}
    public void onEventMainThread(CommonEvent.PingSuccesEvent event)
    {
        headerSuccess();
    }
    public void onEventMainThread(CommonEvent.SuccessConnectEvent event)
    {
    	headerSuccess();
    	Bundle bundle=new Bundle();
    	bundle.putString("topic_name", sTOPIC);
    	connectToService(bundle, Constants.SERVICE_ACTIONs.START_TOPIC.toString());
    }
    public void onEventMainThread(ReceivedMessageEvent event){
		Log.e("onEventMainThread", "ReceivedMessageEvent");
		headerSuccess();
		if(event.getTid().equals(sTOPIC))
		{
			String messagePayLoad=event.getMes();
			User userItem=new User();
			userItem.setName(CommonUtil.extractMessage(messagePayLoad, false));
			userItem.setDatetime(CommonUtil.extractDateMessage(messagePayLoad));
			if(CommonUtil.isEmo(messagePayLoad))
			{
				userItem.setSentEmo(true);
				userItem.setEmoResource(Integer.parseInt(CommonUtil.extractEmoMessage(messagePayLoad, false)));
				userItem.setEmoGroup(Integer.parseInt(CommonUtil.extractEmoMessage(messagePayLoad, true)));
				userItem.setMessage(messagePayLoad);
				lastMsgIcon=userItem.getEmoResource();
				isLastMsgIcon=true;
			}else
			{
				userItem.setMessage(CommonUtil.extractMessage(messagePayLoad, true));
				lastMsg=userItem.getMessage();
			}
			users.add(userItem);
			adapter.notifyDataSetChanged();
			listChat.setSelection(users.size()-1);
		}else
		{
			Log.e("","Message received but to other topic.");
		}
		///Update the numbers of new messages
		UIApplication myApp=(UIApplication)getApplication();
		ArrayList<ChatTopic> topics=myApp.getTopics();
		for(ChatTopic topic:topics)
		{
			if(topic.getTopicId().equals(event.getTid()))
			{
				topic.setNumOfNewMessages(topic.getNumOfNewMessages()+1);
			}
		}
		///
    	for (ChatTopic topic : topics) {
			if(topic.getTopicId().equals(event.getTid()))
			{
				if(isLastMsgIcon)
				{
					topic.setLastMessageIcon(true);
					topic.setLastMessageIcon(lastMsgIcon);
				}else
				{
					topic.setLastMessageIcon(false);
					topic.setLastMessage(lastMsg);
				}
				topic.setTimeOflastMessage(CommonUtil.getCurrentDate());
				break;
			}
		}
    	myApp.setTopics(topics);
	}

	private void headerSuccess() {
		TextView myTitleText = (TextView) findViewById(R.id.headerTitle);
        if ( myTitleText != null ) {
        	if(customTitleSupported)
        	{
        		myTitleText.setText("Appota Chat : "+sTOPIC);// myTitleText.setBackgroundColor(Color.GREEN);
        	}
        }
        ImageView myConnectIcon = (ImageView) findViewById(R.id.connectIcon);
        if(myConnectIcon!=null)
        {
        	if(customTitleSupported)
        	{
        		myConnectIcon.setImageResource(R.drawable.connected);
        	}
        	
        }
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
    	timer.cancel();
    	timer.purge();
    	timer=null;
    	super.onDestroy();
    	
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
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(messageET.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(destinationET.getWindowToken(), 0);
					more.setText("-");
				}
			}
		});
    	Intent receivedIntent=getIntent();
    	destinationET.setText(receivedIntent.getExtras().getString("topic_name"));
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
					final String preTopic=sTOPIC; 
					sTOPIC=destinationET.getText().toString().trim();
					destinationET.setText(sTOPIC);
					Topic[] topics = {new Topic(sTOPIC, QoS.AT_LEAST_ONCE)};
					if(!connection.isConnected())
					{
						connect();
					}
					Log.e("SubscribeTopic", "Subscribing new topic..");
					connection.subscribe(topics).then(onui(new Callback<byte[]>() {
						public void onSuccess(byte[] subscription) {
							connection.unsubscribe(new String[]{preTopic});
							toast("Joined the group!");
							renewListChat();
					    	connectToService(null, Constants.SERVICE_ACTIONs.START_TOPIC.toString());
					    	boolean newTopic=true;
					    	UIApplication myApp=(UIApplication)getApplication();
					    	ArrayList<ChatTopic> topics=myApp.getTopics();
					    	for (ChatTopic topic : topics) {
								if(topic.getTopicId().equals(sTOPIC))
								{
									newTopic=false;
									break;
								}
							}
					    	if(newTopic)
					    	{
					    		ChatTopic topic=new ChatTopic();
					    		topic.setTopicId(sTOPIC);
					    		topic.setTopicName(sTOPIC);
					    		topics.add(topic);
					    		myApp.setTopics(topics);
					    	}
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
    	//read file
	    	String filePath=null;
			filePath=CommonUtil.getFilePath();
			String filename=sTOPIC+".ca";
		
			Log.e("Start Reading H-file", filePath+"/"+filename);
			BufferedReader buff=null;
			try {
				File codefile = new File(filePath+"/"+filename);
				boolean isHistoryOfChatAvailable = false;
				isHistoryOfChatAvailable=codefile.exists();
				if(isHistoryOfChatAvailable)
				{
					FileReader writer = new FileReader(codefile);
					buff=new BufferedReader(writer);
					String messagePayLoad;
					while ((messagePayLoad = buff.readLine()) != null) {
						if(!messagePayLoad.contains("~#@#~"))
						{
							continue;
						}
						User userItem=new User();
						userItem.setName(CommonUtil.extractMessage(messagePayLoad, false));
						userItem.setDatetime(CommonUtil.extractDateMessage(messagePayLoad));
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
				}
		       
			}catch (IOException e) {
				Log.e("Read file", "File read failed: " + e.toString());
			}
		    finally {
				try {
					if (buff != null)buff.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
    	///
//    	handler.postDelayed((new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				connectToService(null, Constants.SERVICE_ACTIONs.PING.toString());
//			}
//		}),1000);
    	Bundle bundle=new Bundle();
    	bundle.putString("topic_name", sTOPIC);
    	connectToService(bundle, Constants.SERVICE_ACTIONs.START_TOPIC.toString());
    }
    private void connectToService(Bundle bundle,String action)
    {
    	Intent startServiceIntent=new Intent(this,MQTTService.class);
    	startServiceIntent.setAction(action);
    	if(bundle!=null)
    	{
    		startServiceIntent.putExtras(bundle);
    	}
    	startService(startServiceIntent);
    }
    private void renewListChat()
    {
    	users=new ArrayList<User>();
    	adapter=new RoomChatAdapter(this, users, clientId);
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
				try {
					send();
				} catch (Exception e) {
					toast("Unable to send message");
				}
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
		Log.e("ActivityConnect","Connecting...");
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
		try {
			connection.connect().then(onui(new Callback<Void>(){
				public void onSuccess(Void value) {
					progressDialog.dismiss();
//					sendButton.setEnabled(true);
					Log.e("Re-Connect", "re-connect sucessful!");
				}
				public void onFailure(Throwable e) {
					toast("Problem connecting to host");
					Log.e(TAG, "Exception connecting to " + sAddress + " - " + e);
					Log.e("Re-Connect", "re-connect failed.");
					progressDialog.dismiss();
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}

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
						Log.e("Disconnected","onPause");
					}
					public void onFailure(Throwable e) {
						toast("Problem disconnecting");
						Log.e(TAG, "Exception disconnecting from " + sAddress + " - " + e);
					}
				}));
			}
			else
			{
//				toast("Not Connected");
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
						Log.e(TAG, "TOPIC: " + sTOPIC);
						Log.e(TAG, "Message: " + sMessage);
						// publish message
						String cMessage=CommonUtil.convertMessage(clientId, sMessage);
						connection.publish(sTOPIC, cMessage.getBytes(), QoS.AT_LEAST_ONCE, false);
//						TOPICET.setText("");
						sentNewMessage=true;
						isLastMsgIcon=false;
						lastMsg=CommonUtil.extractMessage(cMessage, true);
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
						isLastMsgIcon=true;
						lastMsgIcon=Integer.parseInt(CommonUtil.extractEmoMessage(sMessage, false));
						sentNewMessage=true;
						messageET.setText("");
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