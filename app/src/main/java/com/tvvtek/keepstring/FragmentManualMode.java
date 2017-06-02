package com.tvvtek.keepstring;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class FragmentManualMode extends Fragment {
    SharedPreferences sPref;
    private static String userkey = "userkey";
    private static final String TAG = "KeepString";
    EditText input_send_to_clip, input_receive_from_clip;
    Button btnWrite, btnRead;
    ImageView line;
    StaticSettings staticSettings;
    private FragmentActivity myContext;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;
        myContext=(FragmentActivity) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        if (loadCookie().length() == 128){
            return onCreateViewAuthOk(inflater, container, savedInstanceState);
        }
        else {
            return onCreateViewNoAuth(inflater, container, savedInstanceState);
        }
    }
    public View onCreateViewAuthOk(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_manual, container, false);
        // Refresh IU
        line = (ImageView) myView.findViewById(R.id.line);
        btnWrite = (Button)myView.findViewById(R.id.btn_write_clip);
        btnRead = (Button)myView.findViewById(R.id.btn_read_clip);
        input_send_to_clip = (EditText) myView.findViewById(R.id.input_send_to_clip);
        input_receive_from_clip = (EditText) myView.findViewById(R.id.input_receive_from_clip);
        btnWrite.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            if (input_send_to_clip.length() < 6){
                Toast toast = Toast.makeText(getContext(), R.string.error_input_data_write, Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                clipWrite(input_send_to_clip.getText().toString());
                Toast toast = Toast.makeText(getContext(), R.string.input_data_write_manual_ok, Toast.LENGTH_SHORT);
                toast.show();
            }
        }});
        btnRead.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            input_receive_from_clip.setText(clipRead());
        }});
        // other methods for view
        input_send_to_clip.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.input_send_to_clip) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;}}return false;}});
        // other methods for view
        input_receive_from_clip.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.input_receive_from_clip) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;}}return false;}});
        return myView;
    }
    private View onCreateViewNoAuth(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View myView = inflater.inflate(R.layout.fragment_manual, container, false);
        goEnterFragment();
        return myView;
    }

    private void clipWrite(String textforwriteclip){
        try{
            ServiceInterCloud.trigger = true;
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("clip", textforwriteclip);
        clipboard.setPrimaryClip(clip);}
        catch (Exception errorwriteclip){
        }
        //     Log.d(TAG, "write=" + textforwriteclip);
    }
    private String clipRead(){
        try{android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            //     Log.d(TAG, "read=" + item.getText().toString());
            return item.getText().toString();}
        catch (Exception errorclipread){
            return "clip_is_empty";}
    }
    private String loadCookie() {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(userkey, "");
        return savedText;
    }
    /**
     * Section goBack previous fragment
     * Go Enter Fragment
     */
    private void goEnterFragment(){
        Fragment fragment = null;
        Class fragmentClass = null;
        FragmentManager fragManager = myContext.getSupportFragmentManager();
        fragmentClass = FragmentEnter.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            // replace this fragment
            fragManager.beginTransaction().replace(R.id.container, fragment).commit();
        }catch (Exception h){
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}