package com.example.karo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.GridView;

import com.example.karo.adapter.AvatarsAdapter;
import com.example.karo.adapter.GameBoardAdapter;
import com.example.karo.model.Avatar;
import com.example.karo.model.Cell;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;

import java.io.File;
import java.util.ArrayList;

public class AvatarsActivity extends AppCompatActivity implements AvatarsAdapter.IPickedAvatar {

    private AvatarsAdapter avatarsAdapter;
    private GridView gridviewAvatarsSource;
    String imgPickedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatars);
        setupAvatarsAdapter();
    }

    private void setupAvatarsAdapter() {
        ArrayList<Avatar> avatars = new ArrayList<>();
        ContextWrapper contextWrapper = new ContextWrapper(this);
        // path to /data/data/<your_app>/app_images
        File directory = contextWrapper.getDir(Const.DIRECTORY_IMAGES, MODE_PRIVATE);
        for (File file : directory.listFiles()) {
            if (!file.getName().equals(Const.DEFAULT_AVATAR_REF)) {
                Bitmap bitmap = CommonLogic.loadImageFromInternalStorage(file.getPath());
                String ref = file.getName();
                avatars.add(new Avatar(bitmap, ref));
            }
        }

        avatarsAdapter = new AvatarsAdapter(avatars, (AvatarsAdapter.IPickedAvatar) this);
        gridviewAvatarsSource = findViewById(R.id.gridviewAvatarsSource);
        gridviewAvatarsSource.setAdapter(avatarsAdapter);
    }


    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(Const.IMG_PICKED_REF, imgPickedRef);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void setImagePickedRef(String imgPickedRef) {
        this.imgPickedRef = imgPickedRef;
        finish();
    }
}