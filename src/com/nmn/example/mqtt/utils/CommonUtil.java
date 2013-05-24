package com.nmn.example.mqtt.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class CommonUtil {
public static boolean isSDCardPresent() {

	return Environment.getExternalStorageState().equals(
			Environment.MEDIA_MOUNTED);
}
public static String getFilePath()
{
	if(isSDCardPresent())
	{
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/com/appstorevngp/vn/chat/logs";
	}else
	{
		return Environment.getDataDirectory().getAbsolutePath()+"/"+"com/appstorevngp/vn"+"/chat/logs";
	}
	
}

public static String getCurrentDate()
{
	String pattern = "dd-MM-yyyy HH:mm:ss";
    SimpleDateFormat format = new SimpleDateFormat(pattern,Locale.getDefault());
    String datetime="";
    try {
      datetime=format.format(new Date());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return datetime;
}
public static String convertMessage(String clientId,String message)
{
	return clientId+"~#@#~"+message;
}
public static String convertMessage4Emoticon(String clientId,String message,int indexOfEmoGroup)
{
	return clientId+"~#@#~"+indexOfEmoGroup+"~#EM0#~"+message;
}
public static boolean isEmo(String message)
{
	return message.contains("~#EM0#~");
}
public static String extractMessage(String message,boolean getMessage)
{
	return getMessage? message.split("~#@#~")[1]:message.split("~#@#~")[0];
}
public static String extractEmoMessage(String message,boolean getGroup)
{
	String content=extractMessage(message, true);
	return getGroup? content.split("~#EM0#~")[0]:content.split("~#EM0#~")[1];
}
public static boolean isNetworkAvailable(Context context) {
	ConnectivityManager connectivity = (ConnectivityManager) context
			.getSystemService(Context.CONNECTIVITY_SERVICE);
	if (connectivity == null) {
		return false;
	} else {
		NetworkInfo[] info = connectivity.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED
						|| info[i].getState() == NetworkInfo.State.CONNECTING) {
					return true;
				}
			}
		}
	}
	return false;
}
}
