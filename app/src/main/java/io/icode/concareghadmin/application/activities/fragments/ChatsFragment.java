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
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private RecyclerViewAdapterUser recyclerViewAdapterUser;

    private List<Users> mUsers;

    private List<String> usersList;

    FirebaseUser currentUser;

    DatabaseReference reference;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats,container,false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear list
                usersList.clear();
                // gets the senders and receiver data and add to list of chatted users
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    if(chats.getSender().equals(currentUser.getUid())){
                        usersList.add(chats.getReceiver());
                    }
                    if(chats.getReceiver().equals(currentUser.getUid())){
                        usersList.add(chats.getSender());
                    }
                }
                // method call to read user's chats
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


        return view;
    }

    private void readChats(){

        mUsers = new ArrayList<>();

         reference = FirebaseDatabase.getInstance().getReference("Users");

         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                 mUsers.clear();

                 for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                     Users users = snapshot.getValue(Users.class);
                     // displaying 1 users from chats
                     for(String id : usersList){
                         if(users.getUid().equals(id)){
                             if(mUsers.size() != 0){
                                 for(Users users1 : mUsers){
                                     if(!users.getUid().equals(users1.getUid())){
                                         mUsers.add(users);
                                     }
                                 }
                             }
                             else{
                                //mUsers.add(users);
                             }

                         }
                     }

                 }

                 /*
                 for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                     Users users = snapshot.getValue(Users.class);
                     // displaying 1 users from chats
                     for(String id : usersList){
                         if(users.getUid().equals(id)){
                             if(mUsers.size() != 0){
                                 for(Users users1 : mUsers){
                                     if(!users.getUid().equals(users1.getUid())){
                                         mUsers.add(users);
                                     }
                                 }
                             }
                             else{
                                 mUsers.add(users);
                             }
                         }
                     }

                 }
                 */

                 // initializing && setting adapter to recyclerView
                 recyclerViewAdapterUser = new RecyclerViewAdapterUser(getContext(), mUsers);
                 recyclerView.setAdapter(recyclerViewAdapterUser);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
             }
         });


    }

}
