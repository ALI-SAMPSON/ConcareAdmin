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

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.activities.AdminLoginActivity;
import io.icode.concareghadmin.application.activities.adapters.ViewPagerAdapter;
import io.icode.concareghadmin.application.activities.fragments.ChatsFragment;
import io.icode.concareghadmin.application.activities.fragments.UsersFragment;
import io.icode.concareghadmin.application.activities.models.Users;
import maes.tech.intentanim.CustomIntent;

public class HomeActivity extends AppCompatActivity {
    CircleImageView profile_image;
    TextView username;


    Users users;

    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference chatDbRef;

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

        users = new Users();

        user = mAuth.getCurrentUser();

        chatDbRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        chatDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Users users = dataSnapshot.getValue(Users.class);

                username.setText(user.getDisplayName());

                //text if user's imageUrl is equal to default
                if(user.getPhotoUrl() == null){
                    profile_image.setImageResource(R.drawable.app_logo);
                }
                else{
                    // load user's Image Url
                    Glide.with(HomeActivity.this).load(user.getPhotoUrl()).into(profile_image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        // getting reference to the views
        TabLayout tabLayout =  findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adds ChatsFragment and AdminFragment to the viewPager
        viewPagerAdapter.addFragment(new ChatsFragment(), getString(R.string.text_chats));
        viewPagerAdapter.addFragment(new UsersFragment(), getString(R.string.text_users));
        //Sets Adapter view of the ViewPager
        viewPager.setAdapter(viewPagerAdapter);

        //sets tablayout with viewPager
        tabLayout.setupWithViewPager(viewPager);
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

                startActivity(new Intent(HomeActivity.this, AdminLoginActivity.class));

                CustomIntent.customType(HomeActivity.this, "fadein-to-fadeout");

                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
