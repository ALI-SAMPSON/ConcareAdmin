package io.icode.concareghadmin.application.activities.chatApp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.MessageAdapter;
import io.icode.concareghadmin.application.activities.models.Chat;
import io.icode.concareghadmin.application.activities.models.User;

public class MessageActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;

    CircleImageView profile_image;
    TextView username;

    FirebaseUser currentUser;
    DatabaseReference userRef;

    DatabaseReference chatRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageView btn_send;

    Intent intent;

    // string to get intentExtras
    String users_id;
    String users_name;

    // variable for MessageAdapter class
    MessageAdapter messageAdapter;
    List<Chat> mChats;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        relativeLayout = findViewById(R.id.relativeLayout);

        profile_image =  findViewById(R.id.profile_image);
        username =  findViewById(R.id.username);
        msg_to_send =  findViewById(R.id.editText_send);
        btn_send =  findViewById(R.id.btn_send);

        //getting reference to the recyclerview and setting it up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        users_id = intent.getStringExtra("uid");
        users_name = intent.getStringExtra("username");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference("User").child(users_id);

        getUserDetails();

    }

    private void getUserDetails(){

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                if(user.getImageUrl() == null){
                    // sets a default placeholder into imageView if url is null
                    profile_image.setImageResource(R.drawable.ic_person_unknown);
                }
                else{
                    // loads imageUrl into imageView if url is not null
                    Glide.with(MessageActivity.this)
                            .load(user.getImageUrl()).into(profile_image);
                }

                // method call
                readMessages(currentUser.getDisplayName(),users_name, user.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void sendMessage(String sender, String receiver, String message){

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message",message);

        chatRef.child("Chat").push().setValue(hashMap);

    }

    // ImageView OnClickListener to send Message
    public void btnSend(View view) {

        String message  = msg_to_send.getText().toString();

        // checks if the edit field is not message before sending message
        if(!message.equals("")){
            //btn_send.setVisibility(View.VISIBLE);
            // call to method to sendMessage
            sendMessage(currentUser.getDisplayName(),users_name,message);
        }
        else{
            Toast.makeText(MessageActivity.this,
                    "Oops, No message to send",Toast.LENGTH_LONG).show();
        }
        // clear the field after message is sent
        msg_to_send.setText("");
    }

    // method to readMessages from the system
    private void readMessages(final String myid, final String userid, final String imageUrl){

        // array initialization
        mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference("Chat");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChats.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this,mChats,imageUrl);
                    recyclerView.setAdapter(messageAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }
}
