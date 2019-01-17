package io.icode.concareghadmin.application.activities.chatApp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;

public class GroupMessageActivity extends AppCompatActivity {

    Toolbar toolbar;

    CircleImageView groupIcon;
    TextView groupName;

    RecyclerView recyclerView;

    ProgressBar progressBar;

    EditText editTextMessage;

    String group_name;
    String group_image_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);

        // getting reference to ids
        toolbar = findViewById(R.id.toolbar);

        groupIcon =  findViewById(R.id.ci_group_icon);

        groupName =  findViewById(R.id.tv_group_name);

        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        editTextMessage = findViewById(R.id.editTextMessage);

        progressBar = findViewById(R.id.progressBar);

        // getting strings passed from recyclerView adapter
        group_name = getIntent().getStringExtra("group_name");

        group_image_url = getIntent().getStringExtra("group_icon");

        groupName.setText(group_name);

        // display groupIcon and groupName passed to this activity
        displayValues();

    }

    private void displayValues(){

        groupName.setText(group_name);

        if(group_image_url.equals("")){
            // loading default icon as image icon
            Glide.with(GroupMessageActivity.this).load(R.drawable.ic_group).into(groupIcon);
        }
        else{
            // loading default icon as image icon
            Glide.with(GroupMessageActivity.this).load(group_image_url).into(groupIcon);
        }


    }

    public void btnSend(View view) {

    }
}
