package io.icode.concareghadmin.application.activities.chatApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.MessageAdapter;
import io.icode.concareghadmin.application.activities.interfaces.APIService;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Client;
import io.icode.concareghadmin.application.activities.notifications.Data;
import io.icode.concareghadmin.application.activities.notifications.MyResponse;
import io.icode.concareghadmin.application.activities.notifications.Sender;
import io.icode.concareghadmin.application.activities.notifications.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements MessageAdapter.OnItemClickListener {

    RelativeLayout relativeLayout;

    CircleImageView profile_image;
    TextView username;

    // instance of Admin Class
    Admin admin;

    // variable to hold uid of admin from sharePreference
    String admin_uid;

    FirebaseUser currentAdmin;
    DatabaseReference userRef;

    DatabaseReference chatRef;
    DatabaseReference adminRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageButton btn_send;

    Intent intent;

    // string to get intentExtras
    String  users_id;
    String users_name;

    // variable for MessageAdapter class
    MessageAdapter messageAdapter;
    List<Chats> mChats;

    RecyclerView recyclerView;

    // Listener to listener for messages seen
    ValueEventListener seenListener;

    ValueEventListener mDBListener;

    APIService apiService;

    boolean notify = false;

    // loading bar to load messages
    ProgressBar progressBar;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              finish();
            }
        });

        // creates APIService using Google API from the APIService Class
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        relativeLayout = findViewById(R.id.relativeLayout);

        profile_image =  findViewById(R.id.profile_image);
        username =  findViewById(R.id.username);
        msg_to_send =  findViewById(R.id.editTextMessage);
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

        // creating an instance of the Admin Class
        admin = new Admin();

        // getting the uid of the admin stored in sharePreference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        admin_uid = preferences.getString("uid","");

        currentAdmin = FirebaseAuth.getInstance().getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(users_id);

        progressBar =  findViewById(R.id.progressBar);

        // progressDialog to display before deleting message
        progressDialog = new ProgressDialog(this);
        // setting message on progressDialog
        progressDialog.setMessage("Deleting message...");

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
                readMessages(admin_uid,users_id, users.getImageUrl());
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
            sendMessage(admin_uid,users_id,message);
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

        // add chat to the chatList so that it can be added to the Chats fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(admin_uid)
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

        adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(admin_uid);
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                assert admin != null;
                if(notify) {
                  // method call to send notification to user when admin sends a message
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
    private void sendNotification(String receiver, final String username , final String message){
        DatabaseReference tokens  = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(admin_uid, R.mipmap.app_logo_round, username+": "+message,
                            getString(R.string.application_name), users_id);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    // apiService object to sendNotification to user
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
                                    Toast.makeText(MessageActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
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
                    if(chats.getReceiver().equals(admin_uid)
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

        // display progressBar
        progressBar.setVisibility(View.VISIBLE);

        // array initialization
        mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        mDBListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clears the chats to avoid reading duplicate message
                mChats.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    // gets the unique keys of the chats
                    chats.setKey(snapshot.getKey());

                    assert chats != null;
                    if(chats.getReceiver().equals(myid) && chats.getSender().equals(userid) ||
                            chats.getReceiver().equals(userid) && chats.getSender().equals(myid)){
                        mChats.add(chats);
                    }

                    // initializing the messageAdapter and setting adapter to recyclerView
                    messageAdapter = new MessageAdapter(MessageActivity.this,mChats,imageUrl);
                    // setting adapter
                    recyclerView.setAdapter(messageAdapter);
                    // notify data change in adapter
                    messageAdapter.notifyDataSetChanged();

                    // dismiss progressBar
                    progressBar.setVisibility(View.GONE);

                    // setting on OnItemClickListener in this activity as an interface for ContextMenu
                    messageAdapter.setOnItemClickListener(MessageActivity.this);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // dismiss progressBar
                progressBar.setVisibility(View.GONE);

                // display error message
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    /**handling ContextMenu
     Click Listeners in activity
     */

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this," please long click on a message to delete ",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteClick(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setTitle(getString(R.string.title_delete_message));
        builder.setMessage(getString(R.string.text_delete_message));

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // show dialog
                progressDialog.show();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // dismiss dialog
                        progressDialog.dismiss();

                        // gets the position of the selected message
                        Chats selectedMessage = mChats.get(position);

                        //gets the key at the selected position
                        String selectedKey = selectedMessage.getKey();

                        chatRef.child(selectedKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MessageActivity.this," Message deleted ",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                },3000);

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onCancelClick(int position) {
        // do nothing / close ContextMenu
    }

    // keeping track of the current user the admin is chatting to avoid sending notification everytime
    private void currentUser(String users_id){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",users_id);
        editor.apply();
    }

    // setting the status of the users
    private void status(String status){

        adminRef = FirebaseDatabase.getInstance().getReference("Admin")
        .child(admin_uid);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        adminRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        //currentUser(users_id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("online");
        //currentUser("none");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status("offline");
        // removes eventListeners when activity is destroyed
        chatRef.removeEventListener(seenListener);
        chatRef.removeEventListener(mDBListener);
    }
}
