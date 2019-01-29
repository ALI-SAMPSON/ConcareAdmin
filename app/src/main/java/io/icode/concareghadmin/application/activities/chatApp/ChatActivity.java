package io.icode.concareghadmin.application.activities.chatApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.activities.AdminLoginActivity;
import io.icode.concareghadmin.application.activities.adapters.ViewPagerAdapter;
import io.icode.concareghadmin.application.activities.fragments.ChatsFragment;
import io.icode.concareghadmin.application.activities.fragments.GroupsFragment;
import io.icode.concareghadmin.application.activities.fragments.UsersFragment;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Groups;
import io.icode.concareghadmin.application.activities.models.Users;
import maes.tech.intentanim.CustomIntent;

@SuppressWarnings("ALL")
public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;

    RelativeLayout internetConnection;

    LinearLayout linearLayout;

    CircleImageView profile_image;
    TextView username;

    Users users;

    Admin admin;

    Groups groups;

    //check if internet is available or not on phone
    boolean isConnected = false;

    ProgressDialog progressDialog;

    DatabaseReference adminRef;

    DatabaseReference chatRef;

    DatabaseReference groupRef;

    // variable for duration of snackbar and toast
    private static final int DURATION_LONG = 5000;

    private static final int DURATION_SHORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        internetConnection = findViewById(R.id.no_internet_connection);

        linearLayout = findViewById(R.id.linearLayout);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);

        profile_image = findViewById(R.id.profile_image);

        username =  findViewById(R.id.username);

        admin = new Admin();

        users = new Users();

        groups = new Groups();

        groupRef = FirebaseDatabase.getInstance().getReference("Groups");

        // method call to check if internet connection is enabled
        isInternetConnnectionEnabled();

        // method call to change ProgressDialog style based on the android version of user's phone
        changeProgressDialogBackground();

    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_create_group:

                // method call to signout admin
                requestNewGroup();

                break;

            case R.id.menu_sign_out:

                // method call to signout admin
               signOutAdmin();

                break;

            case R.id.menu_exit:

                // finish activity
                finish();

                break;
        }

       return super.onOptionsItemSelected(item);
    }

    // method to check if internet connection is enabled
    private void isInternetConnnectionEnabled(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            //we are connected to a network
            isConnected = true;

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.GONE);

            adminRef = FirebaseDatabase.getInstance().getReference("Admin");

            adminRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Admin admin = snapshot.getValue(Admin.class);
                        assert admin != null;
                        username.setText(admin.getUsername());

                        //text if users's imageUrl is equal to default
                        if (admin.getImageUrl() == null) {
                            profile_image.setImageResource(R.drawable.app_logo);
                        } else {
                            // load users's Image Url
                            Glide.with(ChatActivity.this).load(admin.getImageUrl()).into(profile_image);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // display message if error occurs
                    Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });

            // getting reference to the views
            final TabLayout tabLayout =  findViewById(R.id.tab_layout);
            final ViewPager viewPager = findViewById(R.id.view_pager);

            // Checks for incoming messages and counts them to be displays together in the chats fragments
            chatRef = FirebaseDatabase.getInstance().getReference("Chats");

            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                    // variable to count the number of unread messages
                    int unreadMessages = 0;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chats chats = snapshot.getValue(Chats.class);
                        assert chats != null;
                        if(chats.getReceiver().equals(admin.getAdminUid()) && !chats.isSeen()){
                            unreadMessages++;
                        }
                    }

                    if(unreadMessages == 0){
                        // adds ChatsFragment and AdminFragment to the viewPager
                        viewPagerAdapter.addFragment(new ChatsFragment(), getString(R.string.text_chats));
                    }
                    else{
                        // adds ChatsFragment and AdminFragment to the viewPager + count of unread messages
                        viewPagerAdapter.addFragment(new ChatsFragment(), "("+unreadMessages+") Chats");
                    }

                    // adds UsersFragment and GroupsFragment to the viewPager
                    viewPagerAdapter.addFragment(new GroupsFragment(),getString(R.string.text_groups));
                    viewPagerAdapter.addFragment(new UsersFragment(), getString(R.string.text_users));
                    //Sets Adapter view of the ViewPager
                    viewPager.setAdapter(viewPagerAdapter);

                    //sets tablayout with viewPager
                    tabLayout.setupWithViewPager(viewPager);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });


        }
        // else condition
        else{

            isConnected = false;

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.VISIBLE);
        }

    }

    // request to create new group
    private void requestNewGroup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);

        final EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);

        builder.setTitle(R.string.text_group_name);
        builder.setMessage(R.string.enter_group_name);

        // onclick listener for positive  button
        builder.setPositiveButton(R.string.text_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // getting text from field
                String groupName = editTextGroupName.getText().toString();

                if(TextUtils.isEmpty(groupName)){
                    // display hint to user
                    Toast.makeText(ChatActivity.this, R.string.error_empty_group_name, Toast.LENGTH_SHORT).show();
                }
                else {
                    // create group
                    createNewGroup(groupName);
                }

            }
        });


        // onclick listener for negative button
        builder.setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dismiss / hides the dialog
                dialog.cancel();
            }
        });

        // display the alertDialog
        builder.show();

    }

    // method to create group in database
    private void createNewGroup(final String groupName){

        groups.setGroupName(groupName);

        // checks if group already exist
        groupRef.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    // display hint if group already exist
                    Snackbar.make(linearLayout,groupName + " group already exist . Please create a group with a different name."
                            + " E.g " + groupName + " 01 , 02...",DURATION_LONG).show();

                }

                else {

                    // open users activity so admin can add users

                    Intent intent = new Intent(ChatActivity.this, AddUsersActivity.class);

                    intent.putExtra("group_name",groupName);

                    startActivity(intent);

                    // adds custom animation
                    CustomIntent.customType(ChatActivity.this, "left-to-right");

                    /*groupRef.child(groupName).setValue(groups)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        // display a success message if group is created succcessfully
                                        Snackbar.make(linearLayout,groupName + " group is created successfully ",DURATION_SHORT).show();

                                    }

                                    else {
                                        // display an error message if group is not created succcessfully
                                        Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    */

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display an error message if group is not created succcessfully
                Toast.makeText(ChatActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void signOutAdmin(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(getString(R.string.text_sign_out));
        builder.setMessage(getString(R.string.sign_out_msg));

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // show dialog
                progressDialog.show();

                // delays the running of the ProgressBar for 3 secs
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // dismiss dialog
                        progressDialog.dismiss();

                        // log admin out of the system and clear all stored data
                        clearEmail(ChatActivity.this);

                        // send admin to login activity
                        startActivity(new Intent(ChatActivity.this, AdminLoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                        CustomIntent.customType(ChatActivity.this, "fadein-to-fadeout");

                        finish();

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

    // method to clear sharePreference when admin log outs
    private  void clearEmail(Context ctx){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.clear(); // clear all stored data (email)
        editor.commit();
    }

    private void status(String status){

        adminRef = FirebaseDatabase.getInstance().getReference("Admin");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        adminRef.updateChildren(hashMap);
    }

    // setting status to "online" when activity is resumed

    @Override
    protected void onResume() {
        super.onResume();
        //status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //status("online");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //status("offline");
    }

    // method to change ProgressDialog style based on the android version of user's phone
    private void changeProgressDialogBackground(){

        // if the build sdk version >= android 5.0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //sets the background color according to android version
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("");
            progressDialog.setMessage("signing out...");
        }
        //else do this
        else{
            //sets the background color according to android version
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("");
            progressDialog.setMessage("signing out...");
        }

    }

}
