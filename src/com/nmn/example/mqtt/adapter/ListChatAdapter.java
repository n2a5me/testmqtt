package com.nmn.example.mqtt.adapter;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nmn.example.mqtt.R;
import com.nmn.example.mqtt.model.User;


public class ListChatAdapter extends BaseAdapter {
	public ListChatAdapter(Context context,
			ArrayList<User> users,String clientId) {
		super();
		this.clientId=clientId;
		this.context = context;
		this.users = users;
	}
	
	public ArrayList<User> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}

	private ArrayList<User> users;
	private Context context;
	private String clientId;

	@Override
	public int getCount() {
//		Log.e("Ranking", "total users:"+users.size());
		return users.size();
	}

	@Override
	public User getItem(int position) {

		return users.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.chat_item, null);
			viewHolder = new ViewHolder();
			viewHolder.username = (TextView) convertView
					.findViewById(R.id.username);
			viewHolder.content = (TextView) convertView
					.findViewById(R.id.content);
			viewHolder.datetime = (TextView) convertView
					.findViewById(R.id.datetime);
			viewHolder.layout = (RelativeLayout) convertView
					.findViewById(R.id.layout);
			viewHolder.emo = (ImageView) convertView
					.findViewById(R.id.emo);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
//		Log.e("RankingPosition", position+"");
		//viewHolder.username.setText(users.get(position).getName());
		if(users.get(position).getName().equalsIgnoreCase(clientId))
				{
					viewHolder.layout.setBackgroundResource(R.drawable.bgcolor);
					viewHolder.username.setText("Me");
				}else
				{
					viewHolder.layout.setBackgroundResource(R.drawable.transparent_bg);
					viewHolder.username.setText(users.get(position).getName());
				}
		if(users.get(position).isSentEmo())
		{
			viewHolder.emo.setImageResource(users.get(position).getEmoResource());
			viewHolder.emo.setVisibility(View.VISIBLE);
			viewHolder.content.setVisibility(View.GONE);
		}else
		{
			viewHolder.content.setText(users.get(position).getMessage());
			viewHolder.emo.setVisibility(View.GONE);
			viewHolder.content.setVisibility(View.VISIBLE);
		}
		viewHolder.datetime.setText(users.get(position).getDatetime());
		return convertView;
	}

	public class ViewHolder {
		TextView username,content,datetime;
		ImageView emo;
		RelativeLayout layout;
	}

}
