package com.example.karo.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.karo.R;
import com.example.karo.model.Avatar;
import com.example.karo.model.Cell;

import java.util.ArrayList;

public class AvatarsAdapter extends BaseAdapter {

    ArrayList<Avatar> avatars;
    IPickedAvatar iPickedAvatar;

    public AvatarsAdapter(ArrayList<Avatar> avatars, IPickedAvatar iPickedAvatar) {
        this.avatars = avatars;
        this.iPickedAvatar = iPickedAvatar;
    }

    public void updateData(ArrayList<Avatar> avatars) {
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
        Bitmap bitmap = avatars.get(position).getBitmap();
        avatarView.imgAvatarItem.setImageBitmap(bitmap);
        avatarView.avatarRef = avatars.get(position).getRef();
        return convertView;
    }

    public interface IPickedAvatar {
        void setImagePickedRef(String imgPickedRef);
    }

    public class AvatarView {
        ImageButton imgAvatarItem;
        String avatarRef;

        public AvatarView(View itemView) {
            this.imgAvatarItem = itemView.findViewById(R.id.imgAvatarItem);

            imgAvatarItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iPickedAvatar.setImagePickedRef(avatarRef);
                }
            });
        }
    }
}
