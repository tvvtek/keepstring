package com.tvvtek.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tvvtek.keepstring.R;
import com.tvvtek.keepstring.StaticSettings;

public class FragmentHelpForSlider extends Fragment {
    private Integer num_page = 0;
    Button button_go_web_version;
    StaticSettings staticSettings;

  public void setNumPage(Integer num_page){
      this.num_page = num_page;
  }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup this_view1 = (ViewGroup) inflater.inflate(R.layout.help_view1, container, false);
        ViewGroup this_view2 = (ViewGroup) inflater.inflate(R.layout.help_view2, container, false);
        ViewGroup this_view3 = (ViewGroup) inflater.inflate(R.layout.help_view3, container, false);
        button_go_web_version = (Button)this_view3.findViewById(R.id.button_go_web_version);
        button_go_web_version.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            Intent browserIntent = new
                    Intent(Intent.ACTION_VIEW, Uri.parse(staticSettings.getUrl()));
            startActivity(browserIntent);
        }});
        if (num_page == 0){
            return this_view1;
        } else if(num_page == 1){
            return this_view2;
        } else {
            return this_view3;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}