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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.SavedSharePreference;
import io.icode.concareghadmin.application.activities.activities.AdminLoginActivity;
import io.icode.concareghadmin.application.activities.adapters.ViewPagerAdapter;
import io.icode.concareghadmin.application.activities.fragments.ChatsFragment;
import io.icode.concareghadmin.application.activities.fragments.UsersFragment;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;
import maes.tech.intentanim.CustomIntent;

@SuppressWarnings("ALL")
public class HomeActivity extends AppCompatActivity {

    RelativeLayout internetConnection;

    CircleImageView profile_image;
    TextView username;

    Users users;

    Admin admin;

    ProgressDialog progressDialog;

    DatabaseReference adminRef;

    DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        internetConnection = findViewById(R.id.no_internet_connection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        profile_image = findViewById(R.id.profile_image);

        username =  findViewById(R.id.username);

        admin = new Admin();

        users = new Users();

        //check if internet is available or not on phone
        boolean isConnected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            //we are connected to a network
            isConnected = true;

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.GONE);

            //adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(currentAdmin.getUid());

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
                            Glide.with(getApplicationContext()).load(admin.getImageUrl()).into(profile_image);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(HomeActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
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

                    // adds ChatsFragment and AdminFragment to the viewPager
                    viewPagerAdapter.addFragment(new UsersFragment(), getString(R.string.text_users));
                    //Sets Adapter view of the ViewPager
                    viewPager.setAdapter(viewPagerAdapter);

                    //sets tablayout with viewPager
                    tabLayout.setupWithViewPager(viewPager);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(HomeActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });


        }
        // else condition
        else{

            isConnected = false;

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.VISIBLE);
        }

        // method call to change ProgressDialog style based on the android version of user's phone
        changeProgressDialogBackground();

    }

    @Override
    protected void onStart(){
        super.onStart();

        // checks if user is currently logged in

        /*if(SavedSharePreference.getEmail(HomeActivity.this).length() == 0){

            // start the activity
            startActivity(new Intent(HomeActivity.this,AdminLoginActivity.class));

            // Add a custom animation ot the activity
            CustomIntent.customType(HomeActivity.this,"fadein-to-fadeout");

            // finish the activity
            finish();

        }
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_sign_out:

                // method call to signout admin
               signOutAdmin();

                return true;
        }

        return false;
    }

    private void signOutAdmin(){

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(getString(R.string.text_sign_out));
        builder.setMessage(getString(R.string.sign_out_msg));

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

                        // log admin out of the system and clear all stored data
                        clearEmail(HomeActivity.this);

                        // send admin to login activity
                        startActivity(new Intent(HomeActivity.this, AdminLoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        CustomIntent.customType(HomeActivity.this, "fadein-to-fadeout");

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
