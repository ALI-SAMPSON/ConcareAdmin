package io.icode.concareghadmin.application.activities.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterUser;
import io.icode.concareghadmin.application.activities.models.Users;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ALL")
public class UsersFragment extends Fragment {


    View view;

    private RecyclerView recyclerView;
    private RecyclerViewAdapterUser adapterUser;
    private RecyclerViewAdapterUser adapterSearch;
    private List<Users> mUsers;

    ConstraintLayout mLayout;

    DatabaseReference userRef;

    EditText editTextSearch;

    // material searchView
    MaterialSearchView searchView;

    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_users,container,false);

        mLayout = view.findViewById(R.id.mLayout);

        // getting reference to recyclerview
        recyclerView =  view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        editTextSearch =  view.findViewById(R.id.editTextSearch);

        mUsers = new ArrayList<>();

        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // adapter initialization and RecyclerView set up
        adapterUser = new RecyclerViewAdapterUser(getContext(), mUsers, true);
        // setting adapter to recyclerView
        recyclerView.setAdapter(adapterUser);

        // adapter initialization
        adapterSearch = new RecyclerViewAdapterUser(getContext(),mUsers,false);
        // setting adapter to recyclerView
        recyclerView.setAdapter(adapterSearch);

        // getting reference to progressBar
        progressBar = view.findViewById(R.id.progressBar);

        // method call to display users from db
        displayUsers();

        // method call to search for user
        search();

        // return view
        return view;
    }

    // method calling the searchUsers method
    private void search(){
        // adding TextChange Listener to search edittext
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    // method to search for user in the system
    private void searchUsers(String s) {

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userRef.orderByChild("search")
        .startAt(s)
        .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // clear's list
                mUsers.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Users users = snapshot.getValue(Users.class);

                    assert users != null;

                    mUsers.add(users);

                }

                adapterSearch.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display db error message
                Snackbar.make(mLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    // message to read the admin from the database
    public  void displayUsers(){

        // display the progressBar
        progressBar.setVisibility(View.VISIBLE);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // clear's list
                    mUsers.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Users users = snapshot.getValue(Users.class);

                        assert users != null;

                        mUsers.add(users);

                    }

                    // notifies any data change
                    adapterUser.notifyDataSetChanged();

                // display the progressBar
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // dismiss the progressBar
                progressBar.setVisibility(View.GONE);

                // display db error message
                Snackbar.make(mLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();

            }
        });

    }

}
