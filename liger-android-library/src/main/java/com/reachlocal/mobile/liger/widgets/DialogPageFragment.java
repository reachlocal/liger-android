package com.reachlocal.mobile.liger.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.LoggingChromeClient;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.utils.CordovaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

public class DialogPageFragment extends DialogFragment {

    public static final String TAG = "OkAlertDialogFragment";

    public static final String SAVE_TITLE = "TITLE";
    public static final String SAVE_LINK = "LINK";

    private String title;
    private String link;

    CordovaWebView cwv;

    public DialogPageFragment() {
    }


    @Override
    public void onCreate(Bundle inState) {
        super.onCreate(inState);
        if (inState != null) {
            title = inState.getString(SAVE_TITLE);
            link = inState.getString(SAVE_LINK);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SAVE_TITLE, title);
        outState.putString(SAVE_LINK, link);
    }


    @Override
    public Dialog onCreateDialog(Bundle inState) {
        Activity activity = getActivity();

        cwv = (CordovaWebView) LayoutInflater.from(activity).inflate(R.layout.liger_dialog_content, null, false);
        cwv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        cwv.setWebViewClient(new DialogWebViewClient());
        cwv.setTag(R.id.web_view_parent_dialog, this);
        cwv.setWebChromeClient(new LoggingChromeClient());
        cwv.setOnKeyListener(new DialogKeyListener());

        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogNoFrame);
        Window window = dialog.getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(cwv);

        boolean cancelable = !(StringUtils.equalsIgnoreCase(link, "signin") || StringUtils.equalsIgnoreCase(link, "advertisers"));
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (link != null) {
            cwv.loadUrl(getLinkUrl());
        }
        return dialog;
    }

    private class DialogWebViewClient extends WebViewClient {
        @Override
        public void onLoadResource(WebView view, String url) {
            if (LIGER.LOGGING) {
                Log.d(LIGER.TAG, "DialogPageFragment.onLoadResource() " + url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (LIGER.LOGGING) {
                Log.d(LIGER.TAG, String.format("DialogPageFragment.onReceivedError(), errorCode %d, description: %s, failuingUrl: %s ", errorCode, description, failingUrl));
            }
        }
    }

    private class DialogKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (StringUtils.equalsIgnoreCase(link, "signin")) {
                        getActivity().finish();
                    } else {
                        dismiss();
                    }
                }
                return true;
            }
            return false;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        cwv.handlePause(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        cwv.handleResume(false, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cwv.handleDestroy();
    }

    public String getLinkUrl() {
        if (link == null) {
            return null;
        }
        return "file:///android_asset/app/" + link + ".html";
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getLink() {
        return link;
    }


    public void setLink(String link) {
        this.link = link;
        if (cwv != null) {
            cwv.loadUrl(getLinkUrl());
        }
    }

    public static DialogPageFragment fromCallbackContext(CallbackContext cc) {
        return CordovaUtils.fromCallbackContext(cc, R.id.web_view_parent_dialog);
    }


}
