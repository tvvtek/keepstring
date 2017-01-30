package com.tvvtek.keepstring;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tvvtek.interfaces.InterfaceOnBackPressedListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    StaticSettings staticSettings;
    private static String keyinsidedevice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            savedInstanceState.clear();
        }
        super.onCreate(savedInstanceState);

        try {
            loadKeyFromDeviceFile();
            //  Log.d(TAG, "lengthkey=" + keyinsidedevice.length());
        }catch (Exception error_receive_leng){
            //  Log.d(TAG, "LENG=" + error_receive_leng);
        }

        if (keyinsidedevice.length() == 128){
        }
        else {
            Intent questionIntent = new Intent(this,
                    HelpSliderActivity.class);
            startActivity(questionIntent);
            overridePendingTransition(R.anim.slide_transform,R.anim.alpha);
        }
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // open fragment by default after start app
        navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            InterfaceOnBackPressedListener backPressedListener = null;
            for (Fragment fragment: fm.getFragments()) {
                if (fragment instanceof InterfaceOnBackPressedListener) {
                    backPressedListener = (InterfaceOnBackPressedListener) fragment;
                    break;
                }
            }
            if (backPressedListener != null) {
                backPressedListener.onBackPressed();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        Class fragmentClass = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            fragmentClass = FragmentEnter.class; // переходим на стартовый экран
        } else if (id == R.id.nav_history) {
            fragmentClass = FragmentHistory.class;
        } else if (id == R.id.nav_manual_mode) {
            fragmentClass = FragmentManualMode.class;
        }  else if (id == R.id.nav_pin) {
            fragmentClass = FragmentPinAccess.class; // фрагмент регистрации, входа и включения синхронизации
        } else if (id == R.id.nav_help) {

            Intent questionIntent = new Intent(this,
                    HelpSliderActivity.class);
            startActivity(questionIntent);
            overridePendingTransition(R.anim.slide_transform,R.anim.alpha);

        } else if (id == R.id.menu_item_share) {
            fragmentClass = FragmentShareApp.class;
        } else if (id == R.id.nav_exit) {
            fragmentClass = FragmentExit.class;
        }
        // Жуткий костыль, но некогда было придумывать лучше.
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            item.setChecked(true);
            setTitle(item.getTitle());
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            try {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);}
            catch (Exception error_close_draw){
                error_close_draw.printStackTrace();
            }
            e.printStackTrace();
        }
        return true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void setKey(String key){
        keyinsidedevice = key;
    }
    public void loadKeyFromDeviceFile() {
        String key_into_device;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("key")));
            while ((key_into_device = br.readLine()) != null) {
                //    Log.d(TAG, "fileread=" + g);
                setKey(key_into_device);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
