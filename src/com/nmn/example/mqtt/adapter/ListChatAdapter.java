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
import com.nmn.example.mqtt.model.ChatTopic;
import com.nmn.example.mqtt.model.User;
import com.nmn.example.mqtt.utils.CommonUtil;
import com.readystatesoftware.viewbadger.BadgeView;


public class ListChatAdapter extends BaseAdapter {
	public ListChatAdapter(Context context,
			ArrayList<ChatTopic> topics) {
		super();
		this.context = context;
		this.topics = topics;
	}
	
	public ArrayList<ChatTopic> getUsers() {
		return topics;
	}

	public void setUsers(ArrayList<ChatTopic> topics) {
		this.topics = topics;
	}

	private ArrayList<ChatTopic> topics;
	private Context context;

	@Override
	public int getCount() {
//		Log.e("Ranking", "total users:"+users.size());
		return topics.size();
	}

	@Override
	public ChatTopic getItem(int position) {

		return topics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.chat_center_item, null);
			viewHolder = new ViewHolder();
			viewHolder.groupName = (TextView) convertView
					.findViewById(R.id.groupName);
			viewHolder.lastMessageContent = (TextView) convertView
					.findViewById(R.id.lastMessageContent);
			viewHolder.topicId = (TextView) convertView
					.findViewById(R.id.topicId);
			viewHolder.datetime = (TextView) convertView
					.findViewById(R.id.datetime);
			viewHolder.groupAvatar = (ImageView) convertView
					.findViewById(R.id.groupAvatar);
			viewHolder.lastMessageIcon = (ImageView) convertView
					.findViewById(R.id.lastMessageIcon);
			viewHolder.ripbonAnchor = (ImageView) convertView
					.findViewById(R.id.ripbonAnchor);
			viewHolder.badge=new BadgeView(context, viewHolder.ripbonAnchor);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
//		Log.e("RankingPosition", position+"");
			viewHolder.groupAvatar.setImageResource(R.drawable.matrix_icon);
			viewHolder.groupName.setText(topics.get(position).getTopicId());
			viewHolder.datetime.setText(topics.get(position).getTimeOflastMessage());
			if(topics.get(position).isLastMessageIcon())
			{
				viewHolder.lastMessageIcon.setVisibility(View.VISIBLE);
				viewHolder.lastMessageIcon.setImageResource(topics.get(position).getLastMessageIcon());
				viewHolder.lastMessageContent.setVisibility(View.INVISIBLE);
			}else
			{
				viewHolder.lastMessageContent.setVisibility(View.VISIBLE);
				viewHolder.lastMessageIcon.setVisibility(View.INVISIBLE);
				viewHolder.lastMessageContent.setText(topics.get(position).getLastMessage());
			}
			if(topics.get(position).isReceivedNewMessag())
			{
				viewHolder.ripbonAnchor.setVisibility(View.VISIBLE);
				viewHolder.badge.setText(topics.get(position).getNumOfNewMessages()+"");
				viewHolder.badge.show();
				viewHolder.badge.setVisibility(View.VISIBLE);
			}else
			{
				viewHolder.ripbonAnchor.setVisibility(View.INVISIBLE);
				viewHolder.badge.setVisibility(View.INVISIBLE);
			}
			
			
		return convertView;
	}

	public class ViewHolder {
		TextView groupName,lastMessageContent,topicId,datetime;
		ImageView groupAvatar,lastMessageIcon,ripbonAnchor;
		BadgeView badge;
	}

}
