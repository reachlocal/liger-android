package com.reachlocal.mobile.liger.widgets;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.apache.commons.lang3.StringUtils;

public class AlertDialogFragment extends DialogFragment {

    private String mTitle;
    private String mMessage;
    private String mButtonLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTitle = args.getString("title");
        mMessage = args.getString("message");
        mButtonLabel = args.getString("buttonLabel");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!StringUtils.isEmpty(mTitle)) {
            builder.setTitle(mTitle);
        }
        builder.setMessage(mMessage);
        if (mButtonLabel != null) {
            builder.setPositiveButton(mButtonLabel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });
        }
        return builder.create();
    }

    public static AlertDialogFragment build(String title, String message, String buttonLabel) {
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("buttonLabel", buttonLabel);
        AlertDialogFragment frag = new AlertDialogFragment();
        frag.setArguments(args);
        return frag;
    }
}
