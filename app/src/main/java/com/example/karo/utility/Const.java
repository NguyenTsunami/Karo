package com.example.karo.utility;

import android.graphics.Color;

public class Const {
    public static final String COLLECTION_USERS = "USERS";
    public static final String KEY_CURRENT_USER_DOCUMENT = "document";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_AVATAR_REF = "avatarRef";
    public static final String KEY_SCORE = "score";
    public static final String KEY_AVATARS_SOURCE = "haveAvatarSource";
    public static final String KEY_AVATARS_STORAGE = "avatars";
    public static final String DEFAULT_USERNAME = "Anonymous";
    public static final String DEFAULT_AVATAR_REF = "default_avatar.png";
    public static final int DEFAULT_SCORE = 0;
    public static final String AVATARS_SOURCE_INTERNAL_PATH = "/data/user/0/com.example.karo/app_images/";
    public static final String XML_NAME_CURRENT_USER = "CurrentUser"; //path for cache: data/data/com.example.karo/CurrentUser.xml
    public static final int MODE_LOGIN_FROM_CACHE = 0;
    public static final int MODE_LOGIN_FROM_INPUT = 1;
    public static final int PERMISSION_REQUEST_CODE = 200;
    public static final long MAX_DOWNLOAD_FILE_BYTE = 1024 * 1024;
    public static final int REQUEST_CHANGE_AVATAR = 100;
    public static final int COLOR_DARK_PINK = Color.argb(255, 251, 125, 129);
    public static final int COLOR_LIGHT_PINK = Color.argb(51, 251, 125, 129);
    public static final int COLOR_WHITE = Color.WHITE;
    public static final String DIRECTORY_IMAGES = "images";
    public static final int PLAYER_STATE_NONE = -1;
    public static final int PLAYER_STATE_JOIN_ROOM = 0;
    public static final int PLAYER_STATE_READY = 1;
    public static final String COLLECTION_ROOMS = "ROOMS";
    public static final String KEY_ROOM_DOCUMENT = "roomDocument";
    public static final String KEY_PLAYER_ROLE_X_EMAIL = "playerRoleXEmail";
    public static final String KEY_PLAYER_ROLE_O_EMAIL = "playerRoleOEmail";
    public static final String KEY_PLAYER_ROLE_X_STATE = "playerRoleXState";
    public static final String KEY_PLAYER_ROLE_O_STATE = "playerRoleOState";
    public static final String KEY_PICK_CELL = "pickCell";
    public static final int COLUMN_SIZE = 10;
    public static final int ROW_SIZE = 10;
    public static final String TOKEN_X = "X";
    public static final String TOKEN_O = "O";
    public static final String TOKEN_BLANK = "";
    public static final int ADAPTER_STATE_INSERTED_DATA = 1;
    public static final int ADAPTER_STATE_REMOVED_DATA = -1;
    public static final int ADAPTER_STATE_CHANGED_DATA = 0;
    public static final int WIN_SCORE_EARN = 10;
    public static final String IMG_PICKED_REF = "imgPickRef";
}
