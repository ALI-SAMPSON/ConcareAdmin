package io.icode.concareghadmin.application.activities.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterUser;
import io.icode.concareghadmin.application.activities.chatApp.HomeActivity;
import io.icode.concareghadmin.application.activities.models.Chatlist;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Token;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ALL")
public class ChatsFragment extends Fragment {

    View view;

    TextView tv_no_chats;

    private RecyclerView recyclerView;

    private RecyclerViewAdapterUser recyclerViewAdapterUser;

    private List<Chatlist> usersList;

    private List<Users> mUsers;

    String admin_uid;

    DatabaseReference reference;

    HomeActivity applicationContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = (HomeActivity)context;
    }

    @SuppressWarnings("ALL")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats,container,false);

        tv_no_chats = view.findViewById(R.id.tv_no_chats);

        usersList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(applicationContext));

        // getting the uid of the admin stored in shared preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        admin_uid = preferences.getString("uid","");

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(admin_uid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
                // method call to populate current chats of the chats of the Admin
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
        reference.child(admin_uid).setValue(token1);
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
                            // set visibility to gone
                            tv_no_chats.setVisibility(View.GONE);
                            // sets visibility to Visible if ther are recent chats
                            recyclerView.setVisibility(View.VISIBLE);
                            // adds current users admin has chat with
                            mUsers.add(user);
                        }
                    }
                }

                recyclerViewAdapterUser = new RecyclerViewAdapterUser(applicationContext,mUsers,true);
                recyclerView.setAdapter(recyclerViewAdapterUser);

                // checks if there is no recent chat
                if(!dataSnapshot.exists()){
                    // sets visibility of recyclerView to gone and textView to visible if no recent chat exist
                    recyclerView.setVisibility(View.GONE);
                    tv_no_chats.setVisibility(View.VISIBLE);
                }

                // notifies adapter of any changes
                //recyclerViewAdapterUser.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }



}
