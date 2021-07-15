package com.example.karo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import com.example.karo.R;
import com.example.karo.model.Cell;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

public class GameBoardAdapter extends BaseAdapter {

    private ArrayList<Cell> cells;
    private Context context;
    private boolean isEnablePick;
    private String roomDocument;

    public GameBoardAdapter(Context context, ArrayList<Cell> cells, String roomDocument) {
        this.context = context;
        this.cells = cells;
        this.roomDocument = roomDocument;
        this.isEnablePick = true;
    }

    public void setEnablePick(boolean enablePick) {
        this.isEnablePick = enablePick;
    }

    public void clearCells() {
        for (Cell cell : cells) {
            cell.setToken(Const.TOKEN_BLANK);
            cell.setOnWinLine(false);
        }
        notifyDataSetChanged();
    }

    public void updateToken(int pickCell, String token){
        cells.get(pickCell).setToken(token);
        notifyDataSetChanged();
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    @Override
    public int getCount() {
        return cells.size();
    }

    @Override
    public Object getItem(int position) {
        return cells.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CellView cellView;
        // get view
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_cell, parent, false);
            cellView = new CellView(convertView);
            convertView.setTag(cellView);
        } else {
            cellView = (CellView) convertView.getTag();
        }
        // get data
        Cell cell = cells.get(position);
        // set position
        cellView.position = position;
        // set border
        int xPos = CommonLogic.xPosition(position);
        int yPos = CommonLogic.yPosition(position);
        if (xPos == Const.ROW_SIZE - 1) {
            cellView.imgBorderHorizontal.setVisibility(View.INVISIBLE);
        } else {
            cellView.imgBorderHorizontal.setVisibility(View.VISIBLE);
        }
        if (yPos == Const.COLUMN_SIZE - 1) {
            cellView.imgBorderVertical.setVisibility(View.INVISIBLE);
        } else {
            cellView.imgBorderVertical.setVisibility(View.VISIBLE);
        }
        // set token
        if (cell.getToken().equals(Const.TOKEN_X)) {
            cellView.imgToken.setForeground(context.getResources().getDrawable(R.drawable.icon_x));
        } else if (cell.getToken().equals(Const.TOKEN_O)) {
            cellView.imgToken.setForeground(context.getResources().getDrawable(R.drawable.icon_o));
        } else {
            cellView.imgToken.setForeground(null);
        }
        // set background if on win line
        if (cell.isOnWinLine()) {
            cellView.cardviewCell.setCardBackgroundColor(Const.COLOR_DARK_PINK);
        } else {
            cellView.cardviewCell.setCardBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    public class CellView {
        ImageButton imgToken;
        ImageView imgBorderHorizontal;
        ImageView imgBorderVertical;
        CardView cardviewCell;
        int position;

        public CellView(View itemView) {
            imgToken = itemView.findViewById(R.id.imgToken);
            imgBorderHorizontal = itemView.findViewById(R.id.imgBorderHorizontal);
            imgBorderVertical = itemView.findViewById(R.id.imgBorderVertical);
            cardviewCell = itemView.findViewById(R.id.cardviewCell);
            // handle pick a cell
            imgToken.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEnablePick && cells.get(position).getToken().equals(Const.TOKEN_BLANK)) {
                        isEnablePick = false;
                        sendPickCell(position);
                    }
                }
            });
        }

        private void sendPickCell(int position) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference roomRef = db.collection(Const.COLLECTION_ROOMS).document(roomDocument);
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                transaction.update(roomRef, Const.KEY_PICK_CELL, position);
                return null;
            }).addOnSuccessListener(aVoid -> {

            }).addOnFailureListener(e -> CommonLogic.makeToast(context, "Send state ready failure: " + e.getMessage()));
        }
    }
}
