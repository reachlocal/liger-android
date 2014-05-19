package com.reachlocal.mobile.liger;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import com.reachlocal.mobile.liger.model.ToolbarItemSpec;
import com.reachlocal.mobile.liger.widgets.ToolbarLayout;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CordovaPageFragment extends PageFragment implements ToolbarLayout.OnToolbarItemClickListener {

    DefaultMainActivity activity;
    Menu mMenu;
    MenuInflater mMenuInflater;

    FixedCordovaWebView mWebView;
    ToolbarLayout mToolbarLayout;

    private boolean isLoaded;
    private boolean childArgsSent = false;

    // Fragment arguments
    String pageName;
    String pageArgs;
    boolean isDialog;

    // Instance State
    private String parentUpdateArgs;
    private String toolbarSpec;
    private String childUpdateArgs;
    private String actionBarTitle;
    private boolean canRefresh;

    private List<String> javascriptQueue;

    private List<PageLifecycleListener> mLifecycleListeners = new ArrayList<PageLifecycleListener>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        pageName = args.getString("pageName");
        pageArgs = args.getString("pageArgs");
        actionBarTitle = args.getString("pageTitle");

        if (savedInstanceState != null) {
            parentUpdateArgs = savedInstanceState.getString("parentUpdateArgs");
            toolbarSpec = savedInstanceState.getString("toolbarSpec");
            childUpdateArgs = savedInstanceState.getString("childUpdateArgs");
            actionBarTitle = savedInstanceState.getString("actionBarTitle");
            canRefresh = savedInstanceState.getBoolean("canRefresh");
        }
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.onCreate() " + pageName);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("parentUpdateArgs", parentUpdateArgs);
        outState.putString("toolbarSpec", toolbarSpec);
        outState.putString("childUpdateArgs", childUpdateArgs);
        outState.putString("actionBarTitle", actionBarTitle);
        outState.putBoolean("canRefresh", canRefresh);

    }

    @Override
    public void onDestroy() {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.onDestroy() " + pageName);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.onCreateView() " + pageName);
        }
        if (isDialog) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        return createContentView(inflater, container, savedInstanceState);
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_fragment, container, false);
        mWebView = (FixedCordovaWebView) view.findViewById(R.id.web_view);
        mToolbarLayout = (ToolbarLayout) view.findViewById(R.id.toolbar);

        mWebView.setTag(R.id.web_view_parent_frag, this);
        mWebView.setWebChromeClient(new LoggingChromeClient(activity, mWebView));
        mWebView.setWebViewClient(activity.createLigerWebClient(this, mWebView));
        addJavascriptInterfaces();
        mToolbarLayout.setOnToolbarItemClickListener(this);
        setToolbar(toolbarSpec);

        if (pageName.startsWith("http://") || pageName.startsWith("https://")) {
            mWebView.loadUrl(pageName);
        } else {
            String url = String.format("file:///android_asset/%1$s/%2$s.html", activity.getAssetBaseDirectory(), pageName);
            mWebView.loadUrl(url);
        }
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle inState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogNoFrame);
        isDialog = true;

        View contentView = createContentView(LayoutInflater.from(getActivity()), null, inState);
        mWebView.setOnKeyListener(new DialogKeyListener());

        Window window = dialog.getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        boolean cancelable = !(StringUtils.equalsIgnoreCase(pageName, "signin") || StringUtils.equalsIgnoreCase(pageName, "advertisers"));
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.onDestroyView() " + pageName);
        }
        mWebView.removeAllViews();
        mWebView.handleDestroy();
        mWebView = null;
        mToolbarLayout = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (DefaultMainActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.handleResume(false, false);
        if (!isHidden()) {
            sendChildArgs();
            updateTitle();
            doPageAppear();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.onHiddenChanged() " + pageName + ", " + hidden);
        }
        if (!hidden) {
            sendChildArgs();
            updateTitle();
            doPageAppear();
        }
        for (PageLifecycleListener listener : mLifecycleListeners) {
            listener.onHiddenChanged(this, hidden);
        }
    }


    public void updateTitle() {
        boolean isResumed = isResumed();
        if (isResumed && !isHidden() && !isDialog) {
            activity.setActionBarTitle(actionBarTitle);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.handlePause(false);
    }

    @Override
    public void sendJavascriptWithArgs(String object, String function, String args) {
        String js = String.format("%1$s.%2$s(%3$s);", object, function, args);
        sendJavascript(js);
    }

    @Override
    public void sendJavascript(String js) {
        if (isLoaded) {
            doSendJavascript(js);
        } else {
            queueJavascript(js);
        }
    }

    private void queueJavascript(String js) {
        if(javascriptQueue == null) {
            javascriptQueue = new ArrayList<String>();
        }
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "CordovaPageFragment.queueJavascript() js:" + js);
        }
        javascriptQueue.add(js);
    }

    private void processJavascriptQueue() {
        if(javascriptQueue != null) {
            for (String js : javascriptQueue) {
                doSendJavascript(js);
            }
            javascriptQueue = null;
        }
    }

    private void doSendJavascript(String js) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "CordovaPageFragment.doSendJavascript() to " + (mWebView == null ? "(null webView)" : mWebView.getUrl()) + ", js:" + js);
        }
            mWebView.sendJavascript(js);
    }

    @Override
    public String getPageName() {
        return pageName;
    }

    @Override
    public void setToolbar(String toolbarSpec) {
        this.toolbarSpec = toolbarSpec;
        if (mToolbarLayout != null) {
            if (toolbarSpec == null || toolbarSpec.length() == 0) {
                mToolbarLayout.setVisibility(View.GONE);
                return;
            }
            List<ToolbarItemSpec> specList = ToolbarItemSpec.parseSpecArray(toolbarSpec);
            if (specList == null || specList.size() == 0) {
                mToolbarLayout.setVisibility(View.GONE);
                return;
            }

            mToolbarLayout.setVisibility(View.VISIBLE);
            mToolbarLayout.setToolbarSpecs(specList);
        }
    }

    @Override
    public void onToolbarItemClicked(ToolbarItemSpec item) {
        sendJavascript(item.getCallback());
    }

    @Override
    public void setUserCanRefresh(boolean canRefresh) {
        if (this.canRefresh != canRefresh) {
            this.canRefresh = canRefresh;
            DefaultMainActivity activity = (DefaultMainActivity) getActivity();
            if (activity != null) {
                ActivityCompat.invalidateOptionsMenu(activity);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        mMenuInflater = inflater;
        if (canRefresh && isVisible()) {
            mMenuInflater.inflate(R.menu.refresh, mMenu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.canRefresh && item.getItemId() == R.id.action_refresh) {
            sendJavascript("PAGE.refresh(true);");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void doPageAppear() {
        if (isLoaded) {
            sendJavascript("PAGE.onPageAppear();");
        }
        mWebView.applyAfterMoveFix();
    }

    protected void onPageFinished(WebView webView, String url) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "CordovaPageFragment.onPageFinished() " + pageName + ", " + url);
        }
        isLoaded = true;
        String webTitle = webView.getTitle();
        if (StringUtils.isEmpty(actionBarTitle) && !StringUtils.isEmpty(webTitle)) {
            actionBarTitle = webTitle;
        }
        if (!isHidden()) {
            updateTitle();
        }
        sendChildArgs();
        processJavascriptQueue();
    }

    @Override
    public void doPageClosed() {
        super.doPageClosed();
        for (PageLifecycleListener listener : mLifecycleListeners) {
            listener.onPageClosed(this);
        }
    }

    private void addJavascriptInterfaces() {
        Map<String, Object> interfaces = activity.getJavascriptInterfaces(this);
        Set<String> keys = interfaces.keySet();
        for (String key : keys) {
            Object jsInterface = interfaces.get(key);
            mWebView.addJavascriptInterface(jsInterface, key);
            if (jsInterface instanceof PageLifecycleListener) {
                mLifecycleListeners.add((PageLifecycleListener) jsInterface);
            }
        }
    }

    private void sendChildArgs() {
        if (!StringUtils.isEmpty(childUpdateArgs) && !childArgsSent) {
            childArgsSent = true;
            sendJavascriptWithArgs("PAGE", "childUpdates", childUpdateArgs);
        }
    }

    private void sendPageArgs() {
        if (!StringUtils.isEmpty(pageArgs)) {
            sendJavascriptWithArgs("PAGE", "gotPageArgs", pageArgs);
        }
    }

    @Override
    public void setChildArgs(String childUpdateArgs) {
        this.childUpdateArgs = childUpdateArgs;
        childArgsSent = false;
        if (isLoaded && !isHidden()) {
            sendChildArgs();
        }
    }

    @Override
    public String getPageArgs() {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.getPageArgs() " + pageName);
        }
        return pageArgs;
    }

    @Override
    public String getParentUpdateArgs() {
        return parentUpdateArgs;
    }

    @Override
    public void setParentUpdateArgs(String parentUpdateArgs) {
        this.parentUpdateArgs = parentUpdateArgs;
    }

    private class DialogKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (StringUtils.equalsIgnoreCase(pageName, "signin")) {
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

    public static CordovaPageFragment build(String pageName, String pageTitle, String pageArgs) {
        CordovaPageFragment newFragment = new CordovaPageFragment();
        Bundle args = new Bundle();
        if (pageName != null) {
            args.putString("pageName", pageName);
        }
        if (pageTitle != null) {
            args.putString("pageTitle", pageTitle);
        }
        if (pageArgs != null) {
            args.putString("pageArgs", pageArgs);
        }

        newFragment.setArguments(args);

        return newFragment;
    }

    public static CordovaPageFragment build(String pageName, String pageTitle, JSONObject pageArgs) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString());
    }

}