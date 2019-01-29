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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import io.icode.concareghadmin.application.activities.adapters.GroupMessageAdapter;
import io.icode.concareghadmin.application.activities.adapters.MessageAdapter;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterAddUsers;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.interfaces.APIService;
import io.icode.concareghadmin.application.activities.interfaces.APIServiceGroup;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.GroupChats;
import io.icode.concareghadmin.application.activities.models.Groups;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Client;
import io.icode.concareghadmin.application.activities.notifications.DataGroup;
import io.icode.concareghadmin.application.activities.notifications.MyResponse;
import io.icode.concareghadmin.application.activities.notifications.SenderGroup;
import io.icode.concareghadmin.application.activities.notifications.Token;
import maes.tech.intentanim.CustomIntent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMessageActivity extends AppCompatActivity{

    RelativeLayout relativeLayout;

    Toolbar toolbar;

    CircleImageView groupIcon;
    TextView groupName;

    RecyclerView recyclerView;

    // loading bar to load messages
    ProgressBar progressBar;

    ProgressDialog progressDialog;

    // instance of Admin Class
    Admin admin;

    String group_name;
    String group_image_url;

    String admin_uid;

    DatabaseReference groupRef;
    DatabaseReference adminRef;
    DatabaseReference chatRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageButton btn_send;

    // variable for MessageAdapter class
    GroupMessageAdapter groupMessageAdapter;
    List<GroupChats> mChats;

    private RecyclerViewAdapterAddUsers adapterUsers;
    private List<Users> mUsers;

    // list to get the ids of selected users from group creating
    private List<String> usersIds;

    // Listener to listener for messages seen
    ValueEventListener seenListener;

    ValueEventListener mDBListener;

    APIServiceGroup apiService;

    boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        relativeLayout = findViewById(R.id.relativeLayout);

        // getting reference to ids
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // creates APIService using Google API from the APIService Class
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIServiceGroup.class);

        groupName =  findViewById(R.id.tv_group_name);
        groupIcon =  findViewById(R.id.ci_group_icon);
        msg_to_send =  findViewById(R.id.editTextMessage);
        btn_send =  findViewById(R.id.btn_send);

        //getting reference to the recyclerview and setting it up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        progressBar = findViewById(R.id.progressBar);

        // progressDialog to display before deleting message
        progressDialog = new ProgressDialog(this);
        // setting message on progressDialog
        progressDialog.setMessage("Deleting message...");

        admin = new Admin();

        mUsers = new ArrayList<>();

        // getting strings passed from recyclerView adapter
        group_name = getIntent().getStringExtra("group_name");
        group_image_url = getIntent().getStringExtra("group_icon");
        usersIds = getIntent().getStringArrayListExtra("usersIds");

        adapterUsers = new RecyclerViewAdapterAddUsers(this,mUsers,true);

        // getting the list of ids of selected users

        groupRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_REF).child(group_name);

        // getting the uid of the admin stored in sharePreference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        admin_uid = preferences.getString("uid","");

        // display groupIcon and groupName passed to this activity
        //displayValues();

        getGroupDetails();

        //gettingListOfUsersIds();

        //seenMessage(usersIds);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group_profile,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_group_icon:

                startActivity(new Intent(GroupMessageActivity.this,ChangeGroupIconActivity.class));

                CustomIntent.customType(GroupMessageActivity.this, "fadein-to-fadeout");

                break;

                default:
                    break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getGroupDetails(){

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Groups groups = dataSnapshot.getValue(Groups.class);

                assert groups != null;

                // setting group name
                groupName.setText(groups.getGroupName());

                // setting group icon
                if(group_image_url == null){
                    // loading default icon as image icon
                    Glide.with(GroupMessageActivity.this).load(R.drawable.ic_group).into(groupIcon);
                }
                else{
                    // loading default icon as image icon
                    Glide.with(GroupMessageActivity.this).load(group_image_url).into(groupIcon);
                }

                // method call
                readMessages(admin_uid,usersIds, groups.getGroupIcon());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message if exception occurs
                Toast.makeText(GroupMessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void gettingListOfUsersIds(){

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clear list
                usersIds.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Groups groups = snapshot.getValue(Groups.class);

                    assert groups != null;

                    //usersIds = groups.getGroupMembersIds();

                    usersIds = (List<String>)groups.getGroupMembersIds();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message if exception occurs
                Toast.makeText(GroupMessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void btnSend(View view) {

        // sets notify to true
        notify = true;

        String message  = msg_to_send.getText().toString();

        // checks if the edit field is not message before sending message
        if(!message.equals("")){

            // call to method to sendMessage
            sendMessage(admin_uid,usersIds,message);
        }
        else{
            Toast.makeText(GroupMessageActivity.this,
                    "Oops, No message to send",Toast.LENGTH_LONG).show();
        }
        // clear the field after message is sent
        msg_to_send.setText("");
    }

    // sends message to user by taking in these three parameters
    private void sendMessage(String sender, final List<String> receivers, String message){


        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receivers", receivers);
        hashMap.put("message",message);
        hashMap.put("isseen", false);

        messageRef.child(Constants.GROUP_CHAT_REF).push().setValue(hashMap);


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
                    //sendNotification(receivers, admin.getUsername(), msg);
                }
                // sets notify to false
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message if exception occurs
                Toast.makeText(GroupMessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }


    // sends notification to respective user as soon as message is sent
   /* private void sendNotification(List<String> receivers, final String username , final String message){
        DatabaseReference tokens  = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receivers);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    DataGroup data = new DataGroup(admin_uid, R.mipmap.app_logo_round, username+": "+message,
                            getString(R.string.application_name), usersIds);

                    assert token != null;
                    SenderGroup sender = new SenderGroup(data, token.getToken());

                    // apiService object to sendNotification to user
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            Toast.makeText(GroupMessageActivity.this,"Failed!",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    // display error message
                                    Toast.makeText(GroupMessageActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(GroupMessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    */


    // method to check if user has seen message
    private void seenMessage(final List<String> users_id){

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
    private void readMessages(final String myid, final List<String> usersids, final String imageUrl){

        // display progressBar
        progressBar.setVisibility(View.VISIBLE);

        // array initialization
        mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_CHAT_REF);

        mDBListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){

                }

                // clears the chats to avoid reading duplicate message
                mChats.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    GroupChats groupChats = snapshot.getValue(GroupChats.class);
                    // gets the unique keys of the chats
                    groupChats.setKey(snapshot.getKey());

                    assert groupChats != null;
                    if(groupChats.getReceivers().equals(myid) && groupChats.getSender().equals(usersids) ||
                            groupChats.getReceivers().equals(usersIds) && groupChats.getSender().equals(myid)){
                        mChats.add(groupChats);
                    }



                }

                // initializing the messageAdapter and setting adapter to recyclerView
                groupMessageAdapter = new GroupMessageAdapter(GroupMessageActivity.this,mChats,imageUrl);
                // setting adapter
                recyclerView.setAdapter(groupMessageAdapter);
                // notify data change in adapter
                groupMessageAdapter.notifyDataSetChanged();

                // dismiss progressBar
                progressBar.setVisibility(View.GONE);

                // setting on OnItemClickListener in this activity as an interface for ContextMenu
                //groupMessageAdapter.setOnItemClickListener(getApplicationContext());

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
     ***/

    /*@Override
    public void onItemClick(int position) {
        Toast.makeText(this," please long click on a message to delete ",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteClick(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessageActivity.this);
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
                                    Toast.makeText(GroupMessageActivity.this," Message deleted ",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GroupMessageActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
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
    */

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
        //chatRef.removeEventListener(seenListener);
        //chatRef.removeEventListener(mDBListener);
    }



}
