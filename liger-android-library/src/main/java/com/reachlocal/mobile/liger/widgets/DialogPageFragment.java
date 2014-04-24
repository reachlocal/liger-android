package com.reachlocal.mobile.liger.widgets;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.reachlocal.mobile.liger.*;
import com.reachlocal.mobile.liger.utils.CordovaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;

public class DialogPageFragment extends DialogFragment {

    public static final String DIALOG_TAG = "DialogPageFragment";
    public static final String PAGE_FRAGMENT_TAG = "DialogPageFragment";

    public static final String SAVE_TITLE = "TITLE";
    public static final String SAVE_PAGE_NAME = "PAGE_NAME";
    public static final String SAVE_ARGS = "ARGS";

    private String mTitle;
    private String mPageName;
    private String mArgs;

    FrameLayout mContentFrame;
    CordovaPageFragment mContentFragment;

    public DialogPageFragment() {
    }


    @Override
    public void onCreate(Bundle inState) {
        super.onCreate(inState);

        Bundle args = inState == null ? getArguments() : inState;
        if (args != null) {
            mTitle = args.getString(SAVE_TITLE);
            mPageName = args.getString(SAVE_PAGE_NAME);
            mArgs = args.getString(SAVE_ARGS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SAVE_TITLE, mTitle);
        outState.putString(SAVE_PAGE_NAME, mPageName);
        outState.putSerializable(SAVE_ARGS, mArgs);
    }


    @Override
    public Dialog onCreateDialog(Bundle inState) {
        Activity activity = getActivity();

//        mContentFrame = (FrameLayout) LayoutInflater.from(activity).inflate(R.layout.liger_dialog_content, null, false);
//        mContentFragment = CordovaPageFragment.build(mPageName, mTitle, mArgs);
//
//        cwv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));//
////
//        cwv.setWebViewClient(new DialogWebViewClient());//
//        cwv.setTag(R.id.web_view_parent_dialog, this);//
//        cwv.setWebChromeClient(new LoggingChromeClient());//
//        cwv.setOnKeyListener(new DialogKeyListener());//
////
//        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogNoFrame);//
//        Window window = dialog.getWindow();//
////
//        window.requestFeature(Window.FEATURE_NO_TITLE);//
//        dialog.setContentView(cwv);//
////
//        boolean cancelable = !(StringUtils.equalsIgnoreCase(mPageName, "signin") || StringUtils.equalsIgnoreCase(mPageName, "advertisers"));//
//        dialog.setCancelable(cancelable);//
//        dialog.setCanceledOnTouchOutside(cancelable);//
////
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);//
////
//        if (mPageName != null) {//
//            cwv.loadUrl(getLinkUrl());//
//        }//
//        return dialog
        return null;
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
                    if (StringUtils.equalsIgnoreCase(mPageName, "signin")) {
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
       // cwv.handlePause(false);
    }

    @Override
    public void onResume() {
        super.onResume();
       // cwv.handleResume(false, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       // cwv.handleDestroy();
    }

    public String getLinkUrl() {
        if (mPageName == null) {
            return null;
        }
        return "file:///android_asset/app/" + mPageName + ".html";
    }

    public static DialogPageFragment fromCallbackContext(CallbackContext cc) {
        return CordovaUtils.fromCallbackContext(cc, R.id.web_view_parent_dialog);
    }

    public static DialogPageFragment build(String pageName, String pageTitle, String pageArgs) {
        DialogPageFragment newFragment = new DialogPageFragment();
        Bundle args = new Bundle();
        if (pageName != null) {
            args.putString(SAVE_PAGE_NAME, pageName);
        }
        if (pageTitle != null) {
            args.putString(SAVE_TITLE, pageTitle);
        }
        if (pageArgs != null) {
            args.putString(SAVE_ARGS, pageArgs);
        }

        newFragment.setArguments(args);

        return newFragment;
    }

}
