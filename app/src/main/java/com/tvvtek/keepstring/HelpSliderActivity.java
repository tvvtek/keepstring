package com.tvvtek.keepstring;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

public class HelpSliderActivity extends FragmentActivity {

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    ImageView arrow_back_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            savedInstanceState.clear();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mPager = (ViewPager) findViewById(R.id.pager);
        arrow_back_title = (ImageView) findViewById(R.id.title_bar_back_button);

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void title_bar_back_button(View view) {
        finish();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FragmentHelpForSlider fragment = new FragmentHelpForSlider();
            fragment.setNumPage(position);
            return fragment;
        }
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}