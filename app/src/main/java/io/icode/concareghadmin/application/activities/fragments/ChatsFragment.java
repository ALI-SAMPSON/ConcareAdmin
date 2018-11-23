package io.icode.concareghadmin.application.activities.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.icode.concareghadmin.application.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats,container,false);


        return view;
    }

}
