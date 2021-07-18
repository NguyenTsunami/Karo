package com.example.karo.ui.ranks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karo.HomeActivity;
import com.example.karo.R;
import com.example.karo.adapter.RanksAdapter;
import com.example.karo.model.User;
import com.example.karo.utility.CommonLogic;
import com.example.karo.utility.Const;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class RanksFragment extends Fragment {

    private ArrayList<User> users = new ArrayList<>();
    private User currentUser;
    private ImageButton btnSearchRanker;
    private ImageButton btnReloadRanks;
    private EditText txtSearchName;
    private RanksAdapter adapter;
    private RecyclerView rcvRanks;
    private ListenerRegistration listenerRegistration;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ranks, container, false);

        // load list user
        loadUserList();

        // setup btn
        txtSearchName = root.findViewById(R.id.txtSearchName);
        txtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loadRanks(txtSearchName.getText().toString());
            }
        });
        txtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                return true;
            }
            return false;
        });

        btnSearchRanker = root.findViewById(R.id.btnSearchRanker);
        btnSearchRanker.setOnClickListener(v -> {
            txtSearchName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.showSoftInput(txtSearchName, InputMethodManager.SHOW_IMPLICIT);
        });

        btnReloadRanks = root.findViewById(R.id.btnReloadRanks);
        btnReloadRanks.setOnClickListener(v -> {
            txtSearchName.setText("");
            loadRanks("");
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        });

        // set up recycler view
        rcvRanks = root.findViewById(R.id.rcvRanks);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // get current user
        HomeActivity homeActivity = (HomeActivity) getActivity();
        assert homeActivity != null;
        currentUser = homeActivity.getCurrentUser();

        // set up adapter for recycler view
        adapter = new RanksAdapter(users, currentUser.getEmail());
        rcvRanks.setAdapter(adapter);
        rcvRanks.setLayoutManager(new LinearLayoutManager(getContext()));

        super.onActivityCreated(savedInstanceState);
    }

    private ArrayList<User> findRanker(String searchName) {
        if (searchName.equals("")) {
            return users;
        }
        ArrayList<User> listRanker = new ArrayList<>();
        for (User user : users) {
            if (user.getUsername().toLowerCase().contains(searchName.toLowerCase())) {
                listRanker.add(user);
            }
        }
        return listRanker;
    }

    private void loadRanks(String searchName) {
        if (adapter != null) {
            ArrayList<User> listRanker = findRanker(searchName);
            adapter.setData(listRanker);
            adapter.notifyDataSetChanged();
            txtSearchName.clearFocus();
        }
    }

    public void loadUserList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(Const.COLLECTION_USERS)
                .orderBy(Const.KEY_SCORE, Query.Direction.DESCENDING);
        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                CommonLogic.makeToast(getContext(), error.getMessage());
                return;
            }
            // Get user list
            int rank = 0;
            int score = Integer.MAX_VALUE;
            int count = 0;
            users.clear();
            for (QueryDocumentSnapshot document : value) {
                count++;
                Map<String, Object> map = document.getData();
                User user = new User(
                        Objects.requireNonNull(map.get(Const.KEY_EMAIL)).toString()
                        , Objects.requireNonNull(map.get(Const.KEY_PASSWORD)).toString()
                        , Objects.requireNonNull(map.get(Const.KEY_USERNAME)).toString()
                        , Objects.requireNonNull(map.get(Const.KEY_AVATAR_REF)).toString()
                        , Integer.parseInt(Objects.requireNonNull(map.get(Const.KEY_SCORE)).toString())
                );

                // set rank
                if (user.getScore() < score) {
                    rank = count;
                    score = user.getScore();
                }
                user.setRank(rank);

                // get bitmap avatar
                Bitmap bitmap = CommonLogic.loadImageFromInternalStorage(
                        Const.AVATARS_SOURCE_INTERNAL_PATH + user.getAvatarRef());
                user.setAvatarBitmap(bitmap);

                // add to list
                users.add(user);
                loadRanks(txtSearchName.getText().toString());
            }
        });
    }

    @Override
    public void onDetach() {
        listenerRegistration.remove();
        super.onDetach();
    }
}