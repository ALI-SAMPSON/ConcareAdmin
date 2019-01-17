package io.icode.concareghadmin.application.activities.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import io.icode.concareghadmin.application.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    View view;

    RecyclerView recyclerView;

    List<String> groupList;

    ProgressBar progressBar;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        groupList = new ArrayList<>();

        progressBar = view.findViewById(R.id.progressBar);


        return view;

    }

}
