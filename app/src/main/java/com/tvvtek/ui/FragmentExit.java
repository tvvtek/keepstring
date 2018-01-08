package com.tvvtek.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tvvtek.keepstring.R;

public class FragmentExit extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_exit, container, false);
        getActivity().finish();
        return myView;
    }
}