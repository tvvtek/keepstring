package com.tvvtek.keepstring;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentExit extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_exit, container, false);
        getActivity().finish();
        return myView;
    }
}