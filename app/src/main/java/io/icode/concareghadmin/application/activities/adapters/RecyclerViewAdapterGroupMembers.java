package io.icode.concareghadmin.application.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.chatApp.MessageActivity;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;

public class RecyclerViewAdapterGroupMembers extends RecyclerView.Adapter<RecyclerViewAdapterGroupMembers.ViewHolder> {

    Admin admin;

    // variable to store admin uid from sharePreference
    //String admin_uid;

    private Context mCtx;
    private List<Users> mUsers;


    public RecyclerViewAdapterGroupMembers(Context mCtx, List<Users> mUsers){
        this.mCtx = mCtx;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_items_group_members,parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // gets the positions of the all users
        final Users users = mUsers.get(position);

        // sets username to the text of the textView
        holder.username.setText(users.getUsername());
        // sets username to the text of the textView
        holder.gender.setText(users.getGender());

        if(users.getImageUrl() == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            holder.profile_pic.setImageResource(R.drawable.ic_person_unknown);
        }
        else{
            // loads users image into the ImageView
            Glide.with(mCtx).load(users.getImageUrl()).into(holder.profile_pic);
        }


        // onClickListener for view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // display user name
                Toast.makeText(mCtx, users.getUsername(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        Admin admin;

        CircleImageView profile_pic;
        TextView username;
        TextView gender;


        public ViewHolder(View itemView) {
            super(itemView);

            admin = new Admin();

            profile_pic = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            gender = itemView.findViewById(R.id.gender);
        }
    }

}