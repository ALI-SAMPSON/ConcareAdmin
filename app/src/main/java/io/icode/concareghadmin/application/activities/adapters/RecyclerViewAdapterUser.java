package io.icode.concareghadmin.application.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.chatApp.MessageActivity;
import io.icode.concareghadmin.application.activities.models.Users;

public class RecyclerViewAdapterUser extends RecyclerView.Adapter<RecyclerViewAdapterUser.ViewHolder> {

    private Context mCtx;
    private List<Users> mUsers;

    public RecyclerViewAdapterUser(Context mCtx, List<Users> mUsers){
        this.mCtx = mCtx;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items_users,parent, false);

        return new RecyclerViewAdapterUser.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // gets the positions of the all users
        final Users users = mUsers.get(position);

        // sets username to the text of the textView
        holder.username.setText(users.getUsername());

        if(users.getImageUrl() == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            holder.profile_pic.setImageResource(R.drawable.ic_person_unknown);
        }
        else{
            // loads user image into the ImageView
            Glide.with(mCtx).load(users.getImageUrl()).into(holder.profile_pic);
        }

        // onClickListener for view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // passing adminUid as a string to the MessageActivity
                Intent intent = new Intent(mCtx,MessageActivity.class);
                intent.putExtra("uid",users.getUid());
                intent.putExtra("username",users.getUsername());
                mCtx.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profile_pic;
        TextView username;

        public ViewHolder(View itemView) {
            super(itemView);

            profile_pic = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
        }
    }

}
