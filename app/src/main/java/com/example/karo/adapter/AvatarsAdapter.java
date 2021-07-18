package com.example.karo.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.karo.R;
import com.example.karo.model.Cell;

import java.util.ArrayList;

public class AvatarsAdapter extends BaseAdapter {

    ArrayList<Bitmap> avatars;

    public AvatarsAdapter(ArrayList<Bitmap> avatars) {
        this.avatars = avatars;
    }

    public void updateData(ArrayList<Bitmap> avatars) {
        this.avatars = avatars;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return avatars.size();
    }

    @Override
    public Object getItem(int position) {
        return avatars.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AvatarView avatarView;
        // get view
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_avatar, parent, false);
            avatarView = new AvatarView(convertView);
            convertView.setTag(avatarView);
        } else {
            avatarView = (AvatarView) convertView.getTag();
        }
        // get data
        Bitmap bitmap = avatars.get(position);
        avatarView.imgAvatarItem.setImageBitmap(bitmap);
        return convertView;
    }

    public class AvatarView {
        ImageButton imgAvatarItem;

        public AvatarView(View itemView) {
            this.imgAvatarItem = itemView.findViewById(R.id.imgAvatarItem);
        }
    }
}
