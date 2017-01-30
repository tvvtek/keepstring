package com.tvvtek.keepstring;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class FragmentHistory extends Fragment {

    private static final int CM_OPEN_ID = 1;
    private static final int CM_COPY_ID = 2;
    private static final int CM_DELETE_ID = 3;
    private static String userkey = "userkey";
    SharedPreferences sPref;
    LinearLayout view;
    StaticSettings staticSettings;
    ListView listView;
    EditText editTextSearch;
    HelperFragmentHistoryLocalItemList adapter;
    HelperFragmentHistoryDBWorked db;
    DialogFragment dialog_frg;
    private FragmentActivity myContext;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;
        myContext=(FragmentActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Log.d(TAG, "PREFER=" + loadCookie());
        if (loadCookie().length() == 128){
            return onCreateViewAuthOk(inflater, container, savedInstanceState);
            //  Log.d(TAG, "coki" + loadCookie());
        }
        else {
            return onCreateViewNoAuth(inflater, container, savedInstanceState);
        }
    }
    private View onCreateViewNoAuth(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View myView = inflater.inflate(R.layout.fragment_manual, container, false);
        goEnterFragment();
        return myView;
    }
    public View onCreateViewAuthOk(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null){
            savedInstanceState.clear();
        }
        // -----------------------
        final View myView = inflater.inflate(R.layout.fragment_history_local, container, false);
        db = new HelperFragmentHistoryDBWorked(getContext());
       // Log.d(TAG, "model=" + Build.MODEL);
        listView = (ListView) myView.findViewById(R.id.lv);
        editTextSearch = (EditText) myView.findViewById(R.id.editText);
        adapter = new HelperFragmentHistoryLocalItemList(getActivity(), db.getArrayListItem());
      //  adapter = new HelperFragmentHistoryLocalItemList(getActivity(), values);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        // update listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final int pos = position;
                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.cm_open_id:
                            //    Log.d(TAG, "titleopen=" + adapter.getItem(pos).substring(25,44));
                                db = new HelperFragmentHistoryDBWorked(getContext());
                                dialog_frg = new FragmentDialog();
                                Bundle bundle = new Bundle();
                                bundle.putString("data", db.readDataByDate(adapter.getItem(pos).substring(25,44)));
                              //  Log.d(TAG, "hist_pos=" + pos);
                                dialog_frg.setArguments(bundle);
                                dialog_frg.show(getFragmentManager(), "");
                                return true;
                            case R.id.cm_copy_id:
                                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                                //     Log.d(TAG, "titlecopy=" + adapter.getItem(acmi.position).substring(25,44));
                                db = new HelperFragmentHistoryDBWorked(getContext());
                                clipWrite(db.readDataByDate(adapter.getItem(pos).substring(25,44)));
                            //    Log.d(TAG, "MENUPOS" + pos);
                                Toast toast = Toast.makeText(getContext(), R.string.data_copyed_into_clip, Toast.LENGTH_SHORT);
                                toast.show();
                                return true;
                            case R.id.cm_delete_id:
                                db = new HelperFragmentHistoryDBWorked(getContext());
                                db.removeItem(adapter.getItem(pos).substring(25,44));
                                reCreateThisFragment();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_menu, popup.getMenu());
                popup.show();
            }
        });
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });

        return myView;
    }
    private void reCreateThisFragment(){
        FragmentTransaction thisfragment = getFragmentManager().beginTransaction();
        thisfragment.detach(this).attach(this).commit();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(1, CM_OPEN_ID, 1, R.string.open_full);
        menu.add(2, CM_COPY_ID, 2, R.string.copy_into_clip_item);
        menu.add(3, CM_DELETE_ID, 3, R.string.delete_item);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //    Log.d(TAG, "titledelete=" + adapter.getItem(acmi.position).substring(25,44));
            db = new HelperFragmentHistoryDBWorked(getContext());
            // cut the timestamp
            db.removeItem(adapter.getItem(acmi.position).substring(25,44));
            reCreateThisFragment();
            return true;
        }
        else if (item.getItemId() == CM_OPEN_ID) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
       //       Log.d(TAG, "titleopen=" + adapter.getItem(acmi.position).substring(25,44));
            db = new HelperFragmentHistoryDBWorked(getContext());
            dialog_frg = new FragmentDialog();
            Bundle bundle = new Bundle();
            bundle.putString("data", db.readDataByDate(adapter.getItem(acmi.position).substring(25,44)));
            dialog_frg.setArguments(bundle);
            dialog_frg.show(getFragmentManager(), "");
            return true;
        }
        else if (item.getItemId() == CM_COPY_ID) {
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
       //     Log.d(TAG, "titlecopy=" + adapter.getItem(acmi.position).substring(25,44));
            db = new HelperFragmentHistoryDBWorked(getContext());
            clipWrite(db.readDataByDate(adapter.getItem(acmi.position).substring(25,44)));
       //     Log.d(TAG, "MENUPOS" + acmi.position);
            Toast toast = Toast.makeText(getContext(), R.string.data_copyed_into_clip, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }
        return super.onContextItemSelected(item);
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
        Fragment fragment;
        Class fragmentClass;
        FragmentManager fragManager = myContext.getSupportFragmentManager();
        fragmentClass = FragmentEnter.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragManager.beginTransaction().replace(R.id.container, fragment).commit();
        }catch (Exception h){
        }
    }
}