package io.icode.concareghadmin.application.activities.chatApp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.GroupMessageAdapter;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.interfaces.APIService;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Groups;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Client;
import io.icode.concareghadmin.application.activities.notifications.Data;
import io.icode.concareghadmin.application.activities.notifications.MyResponse;
import io.icode.concareghadmin.application.activities.notifications.Sender;
import io.icode.concareghadmin.application.activities.notifications.Token;
import maes.tech.intentanim.CustomIntent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class GroupMessageActivity extends AppCompatActivity implements GroupMessageAdapter.OnItemClickListener{

    RelativeLayout relativeLayout;

    Toolbar toolbar;

    CircleImageView groupIcon;
    TextView groupName;

    TextView tv_no_chats;

    RecyclerView recyclerView;

    // loading bar to load messages
    ProgressBar progressBar;

    ProgressDialog progressDialog;

    // instance of Admin Class
    Admin admin;

    String group_name;
    String group_image_url;
    String date_created;
    String time_created;


    String admin_uid;

    DatabaseReference groupRef;
    DatabaseReference adminRef;
    DatabaseReference chatRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageButton btn_send;

    // variable for MessageAdapter class
    GroupMessageAdapter groupMessageAdapter;
    List<Chats> mChats;

    List<Users> mUsers;

    // list to get the ids of selected users from group creating
    List<String> usersIds = new ArrayList<>();

    // Listener to listener for messages seen
    ValueEventListener seenListener;

    ValueEventListener mDBListener;

    APIService apiService;

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
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        groupName =  findViewById(R.id.tv_group_name);
        groupIcon =  findViewById(R.id.ci_group_icon);
        msg_to_send =  findViewById(R.id.editTextMessage);
        btn_send =  findViewById(R.id.btn_send);

        tv_no_chats = findViewById(R.id.tv_no_chats);

        //getting reference to the recycler view and setting it up
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

        //usersIds = new ArrayList<>();

        mUsers = new ArrayList<>();

        // getting strings passed from recyclerView adapter
        group_name = getIntent().getStringExtra("group_name");
        group_image_url = getIntent().getStringExtra("group_icon");
        date_created = getIntent().getStringExtra("date_created");
        time_created = getIntent().getStringExtra("time_created");
        usersIds = getIntent().getStringArrayListExtra("usersIds");

        //adapterUsers = new RecyclerViewAdapterAddUsers(this,mUsers,true);

        groupRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_REF).child(group_name);

        // getting the uid of the admin stored in sharePreference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        admin_uid = preferences.getString("uid","");

        // method call to update token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        getGroupDetails();

        seenMessage(usersIds);

    }

    // Update currentAdmin's token
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
        Token token1 = new Token(token);
        reference.child(admin_uid).setValue(token1);
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

                Intent intent = new Intent(GroupMessageActivity.this,GroupInfoActivity.class);
                intent.putExtra("_group_name",group_name);
                intent.putExtra("_group_icon",group_image_url);
                intent.putExtra("_date_created",date_created);
                intent.putExtra("_time_created",time_created);
                intent.putStringArrayListExtra("_usersIds",(ArrayList<String>)usersIds);
                startActivity(intent);

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
                    Glide.with(getApplicationContext()).load(R.mipmap.group_icon).into(groupIcon);
                }
                else{
                    // loading default icon as image icon
                    Glide.with(getApplicationContext()).load(group_image_url).into(groupIcon);
                }

                // method call
                //readGroupMessages(admin_uid,usersIds, groups.getGroupIcon());

                readGroupMessages(admin_uid,usersIds, groups.getGroupIcon());
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
        hashMap.put("receiver","");
        hashMap.put("receivers", new ArrayList<String>(){{addAll(receivers);}});
        hashMap.put("message",message);
        hashMap.put("isseen", false);

        messageRef.child(Constants.CHAT_REF).push().setValue(hashMap);


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
                    sendNotification(receivers, admin.getUsername(), msg);
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


    // sends notification to respective users as soon as message is sent
    private void sendNotification(final List<String> receivers, final String username , final String message){

        /* for loop to loop through the list of users id in
        the group and send the notification accordingly */
        for(final String id : receivers){

            DatabaseReference tokens  = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
            Query query = tokens.orderByKey().equalTo(id);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Token token = snapshot.getValue(Token.class);
                        Data data = new Data(admin_uid, R.mipmap.app_logo_round, username+": "+message,
                                 getString(R.string.application_name), id);

                        assert token != null;
                        Sender sender = new Sender(data, token.getToken());

                        // apiService object to sendNotification to users
                        apiService.sendNotification(sender)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                        if(response.code() == 200){
                                            if(response.body().success != 1){
                                                /*Toast.makeText(GroupMessageActivity.this,"Failed : "
                                                        + !response.isSuccessful(),Toast.LENGTH_LONG).show();*/
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

    }


    // method to check if user has seen message
    private void seenMessage(final List<String> users_id){

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

        seenListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if(chats.getReceiver().equals(admin_uid)
                            && chats.getSender().equals(users_id) && users_id.contains(chats.getSender())){
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
    private void readGroupMessages(final String adminId, final List<String> usersids, final String imageUrl){

        // display progressBar
        progressBar.setVisibility(View.VISIBLE);

        // array initialization
        mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

        mDBListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    // displays text if there are no recent chats
                    tv_no_chats.setVisibility(View.VISIBLE);

                    // hides progressBar
                    progressBar.setVisibility(View.GONE);
                }
                else{

                    // clears the chats to avoid reading duplicate message
                    mChats.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Chats chats = snapshot.getValue(Chats.class);
                        // gets the unique keys of the chats
                        chats.setKey(snapshot.getKey());

                        assert chats != null;

                        if(chats.getReceivers().equals(usersids) && chats.getSender().equals(adminId)){
                            // hides text if there are recent chats
                            tv_no_chats.setVisibility(View.GONE);
                            // add chats to list of chats
                            mChats.add(chats);
                        }

                        // initializing the messageAdapter and setting adapter to recyclerView
                        groupMessageAdapter = new GroupMessageAdapter(GroupMessageActivity.this,mChats,imageUrl);
                        // setting adapter
                        recyclerView.setAdapter(groupMessageAdapter);
                        // notify data change in adapter
                        groupMessageAdapter.notifyDataSetChanged();
                        // hides text if there are recent chats
                        tv_no_chats.setVisibility(View.GONE);
                        // dismiss progressBar
                        progressBar.setVisibility(View.GONE);
                        // setting on OnItemClickListener in this activity as an interface for ContextMenu
                        groupMessageAdapter.setOnItemClickListener(GroupMessageActivity.this);

                    }
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
     ***/

    @Override
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


    // keeping track of the current user the admin is chatting to avoid sending notification everytime
    private void currentUser(String users_id){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",users_id);
        editor.apply();
    }

    // setting the status of the users
    private void status(String status){

        adminRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_REF)
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
