package io.icode.concareghadmin.application.activities.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.Notifications.Token;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterUser;
import io.icode.concareghadmin.application.activities.chatApp.MessageActivity;
import io.icode.concareghadmin.application.activities.models.Chatlist;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ALL")
public class ChatsFragment extends Fragment {

    View view;

    private RecyclerView recyclerView;

    private RecyclerViewAdapterUser recyclerViewAdapterUser;

    private List<Users> mUsers;

    private List<Chatlist> usersList;

    FirebaseUser currentAdmin;

    DatabaseReference reference;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats,container,false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentAdmin = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();


        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentAdmin.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }

                // method call
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        // method call to update token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    // Update currentAdmin's token
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(currentAdmin.getUid()).setValue(token1);
    }

    // method to populate fragment with Admin chats
    private void chatList(){

        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Users user = snapshot.getValue(Users.class);
                    for(Chatlist chatlist : usersList){
                        assert user != null;
                        if(user.getUid().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }

                recyclerViewAdapterUser = new RecyclerViewAdapterUser(getContext(),mUsers,true);
                recyclerView.setAdapter(recyclerViewAdapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }



}
