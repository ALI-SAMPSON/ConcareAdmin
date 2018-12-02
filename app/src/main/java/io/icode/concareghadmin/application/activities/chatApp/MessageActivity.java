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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.Notifications.Client;
import io.icode.concareghadmin.application.activities.Notifications.Data;
import io.icode.concareghadmin.application.activities.Notifications.MyResponse;
import io.icode.concareghadmin.application.activities.Notifications.Sender;
import io.icode.concareghadmin.application.activities.Notifications.Token;
import io.icode.concareghadmin.application.activities.adapters.MessageAdapter;
import io.icode.concareghadmin.application.activities.fragments.APIService;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;
import maes.tech.intentanim.CustomIntent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;

    CircleImageView profile_image;
    TextView username;

    FirebaseUser currentAdmin;
    DatabaseReference userRef;

    DatabaseReference chatRef;
    DatabaseReference adminRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageView btn_send;

    Intent intent;

    // string to get intentExtras
    String users_id;
    String users_name;

    // variable for MessageAdapter class
    MessageAdapter messageAdapter;
    List<Chats> mChats;

    RecyclerView recyclerView;

    // Listener to listener for messages seen
    ValueEventListener seenListener;

    APIService apiService;

    boolean notify = false;

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
                startActivity(new Intent(MessageActivity.this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                CustomIntent.customType(MessageActivity.this, "fadein-to-fadeout");
            }
        });

        // creates APIService using Google API from the APIService Class
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

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

        currentAdmin = FirebaseAuth.getInstance().getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(users_id);

        getUserDetails();

        seenMessage(users_id);

    }

    private void getUserDetails(){

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                    username.setText(users.getUsername());
                if(users.getImageUrl() == null){
                    // sets a default placeholder into imageView if url is null
                    profile_image.setImageResource(R.drawable.ic_person_unknown);
                }
                else{
                    // loads imageUrl into imageView if url is not null
                    Glide.with(getApplicationContext())
                            .load(users.getImageUrl()).into(profile_image);
                }

                // method call
                readMessages(currentAdmin.getUid(),users_id, users.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    // ImageView OnClickListener to send Message
    public void btnSend(View view) {

        // sets notify to true
        notify = true;

        String message  = msg_to_send.getText().toString();

        // checks if the edit field is not message before sending message
        if(!message.equals("")){
            //btn_send.setVisibility(View.VISIBLE);
            // call to method to sendMessage
            sendMessage(currentAdmin.getUid(),users_id,message);
        }
        else{
            Toast.makeText(MessageActivity.this,
                    "Oops, No message to send",Toast.LENGTH_LONG).show();
        }
        // clear the field after message is sent
        msg_to_send.setText("");
    }

    // sends message to user by taking in these three parameters
    private void sendMessage(String sender, final String receiver, String message){

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message",message);
        hashMap.put("isseen", false);

        messageRef.child("Chats").push().setValue(hashMap);

        // add chat to the Chatlist so that it can be added to the Chats fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentAdmin.getUid())
                .child(users_id);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(users_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        // variable to hold the message to be sent
        final String msg = message;

        adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(currentAdmin.getUid());
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                assert admin != null;
                if(notify) {
                    sendNotification(receiver, admin.getUsername(), msg);
                }
                // sets notify to false
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    // sends notification to respective user as soon as message is sent
    private void sendNotification(String receiver, final String username , final String messsage){
        DatabaseReference tokens  = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentAdmin.getUid(), R.mipmap.app_logo_round,"You have a New Message",
                            username+": "+messsage, users_id);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            Toast.makeText(MessageActivity.this,"Failed!",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    // display error message
                                    //Toast.makeText(MessageActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
        });
    }

    // method to check if user has seen message
    private void seenMessage(final String users_id){

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if(chats.getReceiver().equals(currentAdmin.getUid())
                            && chats.getSender().equals(users_id)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });


    }

    // method to readMessages from the database
    private void readMessages(final String myid, final String userid, final String imageUrl){

        // array initialization
        mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if(chats.getReceiver().equals(myid) && chats.getSender().equals(userid) ||
                            chats.getReceiver().equals(userid) && chats.getSender().equals(myid)){
                        mChats.add(chats);
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

    // setting the status of the users
    private void status(String status){

        userRef = FirebaseDatabase.getInstance().getReference("Admin").child(currentAdmin.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        userRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatRef.removeEventListener(seenListener);
        status("offline");
    }
}
