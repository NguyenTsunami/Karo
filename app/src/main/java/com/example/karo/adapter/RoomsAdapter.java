package com.example.karo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karo.R;
import com.example.karo.model.Room;
import com.example.karo.model.RoomDetail;
import com.example.karo.model.User;
import com.example.karo.ui.rooms.RoomsFragment;
import com.example.karo.utility.Const;

import java.util.ArrayList;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {

    private ArrayList<RoomDetail> rooms;
    private User currentUser;
    private ISendStateToRoom iSendStateToRoom;

    public RoomsAdapter(User currentUser, RoomsFragment roomsFragment) {
        this.rooms = new ArrayList<>();
        this.currentUser = currentUser;
        this.iSendStateToRoom = roomsFragment;
    }

    public void addData(RoomDetail roomDetail) {
        this.rooms.add(roomDetail);
        notifyItemInserted(rooms.size() - 1);
    }

    public void removeData(int position) {
        this.rooms.remove(position);
        notifyItemRemoved(position);
    }

    public void changeData(RoomDetail roomDetail, int position) {
        Room oldRoom = this.rooms.get(position).getRoom();
        Room newRoom = roomDetail.getRoom();
        if (oldRoom.getPlayerRoleXState() != newRoom.getPlayerRoleXState()
                && (oldRoom.getPlayerRoleXState() == Const.PLAYER_STATE_NONE
                || newRoom.getPlayerRoleXState() == Const.PLAYER_STATE_NONE)) {
            this.rooms.get(position).setRoomDocument(roomDetail.getRoomDocument());
            this.rooms.get(position).setRoom(roomDetail.getRoom());
            this.rooms.get(position).setUserRoleX(roomDetail.getUserRoleX());
            this.rooms.get(position).setUserRoleO(roomDetail.getUserRoleO());
            notifyItemChanged(position);
        } else if (oldRoom.getPlayerRoleOState() != newRoom.getPlayerRoleOState()
                && (oldRoom.getPlayerRoleOState() == Const.PLAYER_STATE_NONE
                || newRoom.getPlayerRoleOState() == Const.PLAYER_STATE_NONE)) {
            this.rooms.get(position).setRoomDocument(roomDetail.getRoomDocument());
            this.rooms.get(position).setRoom(roomDetail.getRoom());
            this.rooms.get(position).setUserRoleX(roomDetail.getUserRoleX());
            this.rooms.get(position).setUserRoleO(roomDetail.getUserRoleO());
            notifyItemChanged(position);
        }
    }

    public void setData(RoomDetail roomDetail, int state) {
        if (state == Const.ADAPTER_STATE_REMOVED_DATA) {
            int position = findPosition(roomDetail.getRoomDocument());
            removeData(position);
        } else if (state == Const.ADAPTER_STATE_INSERTED_DATA) {
            addData(roomDetail);
        } else if (state == Const.ADAPTER_STATE_CHANGED_DATA) {
            int position = findPosition(roomDetail.getRoomDocument());
            changeData(roomDetail, position);
        }
    }

    private int findPosition(String roomDocument) {
        int index = -1;
        for (RoomDetail roomDetail : rooms) {
            index++;
            if (roomDetail.getRoomDocument().equals(roomDocument)) {
                return index;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomDetail roomDetail = rooms.get(position);
        Room room = roomDetail.getRoom();
        User userRoleX = roomDetail.getUserRoleX();
        User userRoleO = roomDetail.getUserRoleO();
        holder.roomDocument = roomDetail.getRoomDocument();
        if (room.getPlayerRoleXState() == Const.PLAYER_STATE_NONE) {
            holder.imgPlayerRoleXAvatar.setVisibility(View.GONE);
            holder.txtPlayerRoleXName.setVisibility(View.GONE);
            holder.btnJoinRoleX.setVisibility(View.VISIBLE);
        } else {
            holder.imgPlayerRoleXAvatar.setVisibility(View.VISIBLE);
            holder.txtPlayerRoleXName.setVisibility(View.VISIBLE);
            holder.btnJoinRoleX.setVisibility(View.GONE);
            if (userRoleX != null) {
                holder.txtPlayerRoleXName.setText(userRoleX.getUsername());
                holder.imgPlayerRoleXAvatar.setImageBitmap(userRoleX.getAvatarBitmap());
            }
        }
        if (room.getPlayerRoleOState() == Const.PLAYER_STATE_NONE) {
            holder.imgPlayerRoleOAvatar.setVisibility(View.GONE);
            holder.txtPlayerRoleOName.setVisibility(View.GONE);
            holder.btnJoinRoleO.setVisibility(View.VISIBLE);
        } else {
            holder.imgPlayerRoleOAvatar.setVisibility(View.VISIBLE);
            holder.txtPlayerRoleOName.setVisibility(View.VISIBLE);
            holder.btnJoinRoleO.setVisibility(View.GONE);
            if (userRoleO != null) {
                holder.txtPlayerRoleOName.setText(userRoleO.getUsername());
                holder.imgPlayerRoleOAvatar.setImageBitmap(userRoleO.getAvatarBitmap());
            }
        }
    }


    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public interface ISendStateToRoom {
        void sendStateToRoom(String roomDocument, String role, int state, String email);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlayerRoleXAvatar;
        ImageView imgPlayerRoleOAvatar;
        TextView txtPlayerRoleXName;
        TextView txtPlayerRoleOName;
        Button btnJoinRoleX;
        Button btnJoinRoleO;
        String roomDocument;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPlayerRoleXAvatar = itemView.findViewById(R.id.imgPlayerRoleXAvatar);
            imgPlayerRoleOAvatar = itemView.findViewById(R.id.imgPlayerRoleOAvatar);
            txtPlayerRoleXName = itemView.findViewById(R.id.txtPlayerRoleXName);
            txtPlayerRoleOName = itemView.findViewById(R.id.txtPlayerRoleOName);
            btnJoinRoleX = itemView.findViewById(R.id.btnJoinRoleX);
            btnJoinRoleO = itemView.findViewById(R.id.btnJoinRoleO);

            btnJoinRoleX.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iSendStateToRoom.sendStateToRoom(roomDocument, Const.TOKEN_X,
                            Const.PLAYER_STATE_JOIN_ROOM, currentUser.getEmail());
                }
            });

            btnJoinRoleO.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iSendStateToRoom.sendStateToRoom(roomDocument, Const.TOKEN_O,
                            Const.PLAYER_STATE_JOIN_ROOM, currentUser.getEmail());
                }
            });
        }
    }
}
