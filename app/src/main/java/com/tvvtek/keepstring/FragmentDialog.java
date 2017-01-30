package com.tvvtek.keepstring;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentDialog extends DialogFragment {
    StaticSettings staticSettings;
    TextView dialog_text_data;
    ImageView image_CopyDialogTextToClip, image_CloseDialog;
    private String data_for_dialog;
    private int position_space;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog, null);
        dialog_text_data = (TextView)v.findViewById(R.id.dialog_text_data);
        image_CopyDialogTextToClip = (ImageView) v.findViewById(R.id.image_CopyDialogTextToClip);
        image_CloseDialog = (ImageView) v.findViewById(R.id.image_CloseDialog);

        Bundle bundle = getArguments();
        data_for_dialog = bundle.getString("data");
        dialog_text_data.setText(data_for_dialog);
        dialog_text_data.setOnClickListener(new View.OnClickListener() {public void onClick(View myView) {
    //        Log.d(staticSettings.getLogTag(), "dialog_text_data PRESS");
            try {
                position_space = data_for_dialog.indexOf(" ");
     //           Log.d(staticSettings.getLogTag(), "position_space=" + position_space);
            }
            catch (StringIndexOutOfBoundsException error_find_space){
                error_find_space.printStackTrace();
      //          Log.d(staticSettings.getLogTag(),"error_find_space=" + error_find_space);
            }
            if (data_for_dialog.substring(0, 4).equals("http")) {
                if (position_space != -1) {
                    startBrowser(data_for_dialog.substring(0, position_space));
                    dialog_text_data.setTextColor(getResources().getColor(R.color.colorTextLink));
                }
                else {
                    startBrowser(data_for_dialog);
                    dialog_text_data.setTextColor(getResources().getColor(R.color.colorTextLink));
                }
            }
        }});

        image_CopyDialogTextToClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clipWrite(dialog_text_data.getText().toString());
                Toast toast = Toast.makeText(getContext(), R.string.data_copyed_into_clip, Toast.LENGTH_SHORT);
                toast.show();
                FragmentDialog.super.onCancel(getDialog());
                FragmentDialog.super.onDismiss(getDialog());
            }
        });
        image_CloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentDialog.super.onCancel(getDialog());
                FragmentDialog.super.onDismiss(getDialog());
            }
        });
        return v;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) throws NullPointerException {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
     //   Log.d(staticSettings.getLogTag(), "from activity print fragment=" + myValue);
        return dialog;
    }

    @Override
    public void onResume() throws NullPointerException
    {
        super.onResume();
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Dialog);
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
       // Log.d(staticSettings.getLogTag(), "Dialog 1: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
      //  Log.d(staticSettings.getLogTag(), "Dialog 1: onCancel");
    }

    private void clipWrite(String text_for_write_clip){
        try{
            ServiceInterCloud.trigger = true;
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("clip", text_for_write_clip);
            clipboard.setPrimaryClip(clip);}
        catch (Exception error_write_clip){
            error_write_clip.printStackTrace();
        }
        //     Log.d(TAG, "write=" + textforwriteclip);
    }
    private void startBrowser(String link){
        Intent startBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(startBrowserIntent);
    }
}
