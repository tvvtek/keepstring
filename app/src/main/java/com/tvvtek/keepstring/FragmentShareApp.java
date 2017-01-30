package com.tvvtek.keepstring;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentShareApp extends Fragment {
    StaticSettings staticSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_exit, container, false);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Work easier with clipboard manager! https://play.google.com/store/apps/details?id=com.tvvtek.keepstring");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
        getActivity().recreate();
        return myView;
    }
}