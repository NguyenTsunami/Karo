package com.example.karo.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karo.R;
import com.example.karo.RoomActivity;
import com.example.karo.model.Room;
import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RanksAdapter extends RecyclerView.Adapter<RanksAdapter.ViewHolder> {

    private ArrayList<User> users;
    private String currentUserEmail;

    public RanksAdapter(ArrayList<User> users, String currentUserEmail) {
        this.users = users;
        this.currentUserEmail = currentUserEmail;
    }

    public void setData(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ranker, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        // set up view holder
        holder.txtRankerUsername.setText(user.getUsername());
        holder.txtRankerInfo.setText(String.format("Score: %d - Rank: %d", user.getScore(), user.getRank()));
        holder.imgRankerAvatar.setImageBitmap(user.getAvatarBitmap());

        // hide button Fight! on item of current user
        if (user.getEmail().equals(currentUserEmail)) {
            holder.btnRankerFight.setVisibility(View.GONE);
        } else {
            holder.btnRankerFight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtRankerUsername;
        TextView txtRankerInfo;
        ImageView imgRankerAvatar;
        ImageButton btnRankerFight;
        ConstraintLayout cvRankerBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRankerUsername = itemView.findViewById(R.id.txtRankerUsername);
            txtRankerInfo = itemView.findViewById(R.id.txtRankerInfo);
            imgRankerAvatar = itemView.findViewById(R.id.imgRankerAvatar);
            btnRankerFight = itemView.findViewById(R.id.btnRankerFight);
            cvRankerBackground = itemView.findViewById(R.id.cvRankerBackground);

            // when current user click Fight! to somebody
            btnRankerFight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // create new room
                    CommonLogic.createRoom(itemView.getContext(), currentUserEmail);
                }
            });
        }
    }

}
