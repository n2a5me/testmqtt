package com.nmn.example.mqtt.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.nmn.example.mqtt.R;
import com.nmn.example.mqtt.events.MessageEvent;
import com.nmn.example.mqtt.utils.CommonUtil;

import de.greenrobot.event.EventBus;


public class EmoticonsAdapter  extends BaseAdapter {
	public EmoticonsAdapter(Context c,int emowidth) {
        mContext = c;
        this.emowidth=emowidth;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(emowidth,emowidth));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imageView.startAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.hyperspace_out));
//				Toast.makeText(mContext, "Resource ID"+mThumbIds[position], Toast.LENGTH_SHORT).show();
				MessageEvent mes=new MessageEvent();
				mes.setMessage(mThumbIds[position]+"");
				mes.setGroup(0);
				EventBus.getDefault().post(mes);
			}
		});
        return imageView;
    }

    private Context mContext;
    private int emowidth;
    private Integer[] mThumbIds = {
    		R.drawable.ah_icon,
    		R.drawable.amazed_icon,R.drawable.bad_smelly_icon,
    		R.drawable.beauty_icon,R.drawable.cry_icon,
    		R.drawable.doubt_icon,R.drawable.embarrassed_icon,
    		R.drawable.hungry_icon,R.drawable.matrix_icon,
    		R.drawable.smile_icon,R.drawable.smileicon,
    		R.drawable.still_dreaming_icon,R.drawable.surrender_icon,
    		R.drawable.too_sad_icon,
    		
           
    };
}
