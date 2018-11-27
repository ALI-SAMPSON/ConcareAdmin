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

import java.util.ArrayList;
import java.util.List;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterUser;
import io.icode.concareghadmin.application.activities.models.Chat;
import io.icode.concareghadmin.application.activities.models.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private RecyclerViewAdapterUser recyclerViewAdapterUser;

    private List<User> mUser;

    private List<String> usersList;

    FirebaseUser user;

    DatabaseReference reference;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats,container,false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUser = new ArrayList<>();

        usersList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("Chat");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                // gets the senders and receiver data and add to list of chatted users
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getSender().equals(user.getDisplayName())){
                        usersList.add(chat.getReceiver());
                    }
                    if(chat.getReceiver().equals(user.getDisplayName())){
                        usersList.add(chat.getSender());
                    }
                }
                // method call to read user's chats
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    private void readChats(){

        mUser = new ArrayList<>();

         reference = FirebaseDatabase.getInstance().getReference("User");

         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 mUser.clear();
                 for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                     User user = snapshot.getValue(User.class);

                     // displaying 1 user from chats
                     for(String id : usersList){
                         if(user.getUsername().equals(id)){
                             if(mUser.size() != 0){
                                 for(User user1 : mUser){
                                     if(!user.getUsername().equals(user1.getUsername())){
                                         mUser.add(user);
                                     }
                                 }
                             }
                             else{
                                 mUser.add(user);
                             }
                         }
                     }

                 }

                 // initializing && setting adapter to recyclerView
                 recyclerViewAdapterUser = new RecyclerViewAdapterUser(getContext(),mUser);
                 recyclerView.setAdapter(recyclerViewAdapterUser);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
             }
         });


    }

}
