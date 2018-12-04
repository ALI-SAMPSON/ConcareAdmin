package io.icode.concareghadmin.application.activities.chatApp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.activities.AdminLoginActivity;
import io.icode.concareghadmin.application.activities.adapters.ViewPagerAdapter;
import io.icode.concareghadmin.application.activities.fragments.ChatsFragment;
import io.icode.concareghadmin.application.activities.fragments.UsersFragment;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;
import maes.tech.intentanim.CustomIntent;

public class HomeActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    Users users;

    Admin admin;

    FirebaseAuth mAuth;
    FirebaseUser currentAdmin;
    DatabaseReference adminRef;

    DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        profile_image = findViewById(R.id.profile_image);

        username =  findViewById(R.id.username);

        mAuth = FirebaseAuth.getInstance();

        admin = new Admin();

        users = new Users();

        currentAdmin = mAuth.getCurrentUser();

        adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(currentAdmin.getUid());
        //.child(users.getUid());

        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Admin admin = dataSnapshot.getValue(Admin.class);

                username.setText(admin.getUsername());

                //text if users's imageUrl is equal to default
                if(admin.getImageUrl() == null){
                    profile_image.setImageResource(R.drawable.app_logo);
                }
                else{
                    // load users's Image Url
                    Glide.with(getApplicationContext()).load(admin.getImageUrl()).into(profile_image);
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
                    if(chats.getReceiver().equals(currentAdmin.getUid()) && !chats.isSeen()){
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
                //viewPagerAdapter.addFragment(new ChatsFragment(), getString(R.string.text_chats));
                viewPagerAdapter.addFragment(new UsersFragment(), getString(R.string.text_users));
                //Sets Adapter view of the ViewPager
                viewPager.setAdapter(viewPagerAdapter);

                //sets tablayout with viewPager
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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

                mAuth.signOut();
                // code changed because app will crash
                startActivity(new Intent(HomeActivity.this, AdminLoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                CustomIntent.customType(HomeActivity.this, "fadein-to-fadeout");

                return true;
        }

        return false;
    }

    private void status(String status){

        adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(currentAdmin.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        adminRef.updateChildren(hashMap);
    }

    // setting status to "online" when activity is resumed

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}
