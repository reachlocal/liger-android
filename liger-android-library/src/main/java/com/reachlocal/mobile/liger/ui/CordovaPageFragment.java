package com.reachlocal.mobile.liger.ui;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.listeners.PageLifecycleListener;
import com.reachlocal.mobile.liger.model.ToolbarItemSpec;
import com.reachlocal.mobile.liger.utils.JSUtils;
import com.reachlocal.mobile.liger.utils.JsonUtils;
import com.reachlocal.mobile.liger.widgets.ToolbarLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CordovaPageFragment extends PageFragment implements ToolbarLayout.OnToolbarItemClickListener {

    Menu mMenu;
    MenuInflater mMenuInflater;
    View mWebViewHolder;
    FixedCordovaWebView mWebView;
    ToolbarLayout mToolbarLayout;

    private boolean isLoaded;
    private boolean childArgsSent = false;

    // Fragment arguments
    String pageArgs;
    String pageOptions;
    boolean isDialog;

    // Instance State
    private String parentUpdateArgs;
    private String toolbarSpec;
    private String childUpdateArgs;
    private String actionBarTitle;
    private boolean canRefresh;

    private List<String> javascriptQueue;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        pageName = args.getString("pageName");
        pageArgs = args.getString("pageArgs");
        pageOptions = args.getString("pageOptions");
        actionBarTitle = args.getString("pageTitle");

        if (savedInstanceState != null) {
            parentUpdateArgs = savedInstanceState.getString("parentUpdateArgs");
            toolbarSpec = savedInstanceState.getString("toolbarSpec");
            childUpdateArgs = savedInstanceState.getString("childUpdateArgs");
            actionBarTitle = savedInstanceState.getString("actionBarTitle");
            canRefresh = savedInstanceState.getBoolean("canRefresh");
        }
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".onCreate() " + pageName);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".onCreateView() " + pageName);
        }
        if (isDialog) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        return createContentView(inflater, container, savedInstanceState);
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mWebViewHolder == null){
            mWebViewHolder = inflater.inflate(R.layout.page_fragment, container, false);

            DefaultMainActivity activity = (DefaultMainActivity) mContext;
            ConfigXmlParser parser = new ConfigXmlParser();
            parser.parse(activity);

            CordovaInterface cordova = (CordovaInterface) activity;

            mWebView = (FixedCordovaWebView) mWebViewHolder.findViewById(R.id.web_view);
            mToolbarLayout = (ToolbarLayout) mWebViewHolder.findViewById(R.id.toolbar);

            mWebView.setTag(R.id.web_view_parent_frag, this);

            mWebView.init(cordova, activity.createLigerWebClient(this, mWebView), new LoggingChromeClient(activity, mWebView), parser.getPluginEntries(), parser.getInternalWhitelist(), parser.getExternalWhitelist(),
                    parser.getPreferences() );

            addJavascriptInterfaces();
            mToolbarLayout.setOnToolbarItemClickListener(this);
            setToolbar(toolbarSpec);

            if (pageName.startsWith("http://") || pageName.startsWith("https://")) {
                mWebView.loadUrl(pageName);
            } else {
                String url = String.format("file:///android_asset/%1$s/%2$s.html", activity.getAssetBaseDirectory(), pageName);
                mWebView.loadUrl(url);
            }
        }else{
            View child = mWebViewHolder.findViewById(R.id.web_fragment_container);
            ((ViewGroup) child.getParent()).removeView(child);
        }
        updateTitle();
        return mWebViewHolder;
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
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".onDestroyView() " + pageName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DefaultMainActivity) getActivity()).setMenuSelection(pageName);
        if (mWebView != null)
            mWebView.handleResume(false, false);
        if (!isHidden()) {
            sendChildArgs();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".onHiddenChanged() " + pageName + ", " + hidden);
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

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    public void updateTitle() {
        DefaultMainActivity activity = (DefaultMainActivity) mContext;
        boolean isResumed = isResumed();
        activity.setActionBarTitle(actionBarTitle);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null)
            mWebView.handlePause(false);
    }

    public void sendJavascriptWithArgs(String object, String function, String args) {
        String js = String.format("%1$s.%2$s(%3$s);", object, function, args);
        sendJavascript(js);
    }

    public void sendJavascript(String js) {
        if (isLoaded) {
            doSendJavascript(js);
        } else {
            queueJavascript(js);
        }
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {
        return null;
    }

    @Override
    protected PageFragment getChildPage() {
        throw new RuntimeException("CordovaPageFragment should never get here");
    }

    @Override
    public void closeDialogArguments(String args) {
        sendJavascriptWithArgs("PAGE", "closeDialogArguments", args);
    }

    @Override
    public void pushNotificationTokenUpdated(String registrationId, String errorMessage) {
        String args = JSUtils.stringListToArgString(registrationId, "android", errorMessage);
        sendJavascriptWithArgs("PAGE", "pushNotificationTokenUpdated", args);
    }


    public void queueJavascript(String js) {
        if (javascriptQueue == null) {
            javascriptQueue = new ArrayList<String>();
        }
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".queueJavascript() js:" + js);
        }
        javascriptQueue.add(js);
    }

    protected void processJavascriptQueue() {
        if (javascriptQueue != null) {
            for (String js : javascriptQueue) {
                doSendJavascript(js);
            }
            javascriptQueue = null;
        }
    }

    protected void doSendJavascript(String js) {
        if (LIGER.LOGGING) {
            //Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".doSendJavascript() to " + (mWebView == null ? "(null webView)" : mWebView.getUrl()) + ", js:" + js);
        }
        if (mWebView != null)
            mWebView.sendJavascript(js);
    }

    @Override
    public String getPageName() {
        return pageName;
    }

    @Override
    public String getPageTitle() {
        return actionBarTitle;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        mMenuInflater = inflater;
        // Added !menu.hasVisibleItems() to ensure refresh is not added twice
        if (canRefresh && isVisible() && !menu.hasVisibleItems()) {
            mMenuInflater.inflate(R.menu.liger_refresh, mMenu);
        }
        String rightButtonName = JsonUtils.getRightButtonName(pageOptions);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "rightButtonName: " + rightButtonName);
        }
        if (StringUtils.equalsIgnoreCase(rightButtonName, "save")) {
            mMenuInflater.inflate(R.menu.liger_save, mMenu);
            addHeaderButtonListener(menu, R.id.liger_action_save, rightButtonName);
        }
        if (StringUtils.equalsIgnoreCase(rightButtonName, "search")) {
            mMenuInflater.inflate(R.menu.liger_search, mMenu);
            addHeaderButtonListener(menu, R.id.liger_action_search, rightButtonName);
        }
    }

    private void addHeaderButtonListener(Menu menu, int id, final String buttonName) {
        MenuItem item = menu.findItem(id);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (LIGER.LOGGING) {
                    Log.d(LIGER.TAG, "Header Button clicked: " + buttonName);
                }
                sendJavascriptWithArgs("PAGE", "headerButtonTapped", "\"" + buttonName + "\"");
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (this.canRefresh && itemId == R.id.liger_action_refresh) {
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
        if (mWebView != null)
            mWebView.applyAfterMoveFix();
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        updateTitle();
    }

    protected void onPageFinished(WebView webView, String url) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".onPageFinished() " + pageName + ", " + url);
        }
        for (PageLifecycleListener listener : mLifecycleListeners) {
            listener.onPageFinished(this);
        }
        isLoaded = true;
        String webTitle = webView.getTitle();
        if (StringUtils.isEmpty(actionBarTitle) && !StringUtils.isEmpty(webTitle)) {
            actionBarTitle = webTitle;
        }
        if (!isHidden()) {
            doPageAppear();
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

    @Override
    protected void fragmentDetached(PageFragment detachedFrag) {
        if (mActivity != null) {
            mActivity.onFragmentFinished(this);
        }
    }

    private void addJavascriptInterfaces() {
        DefaultMainActivity activity = (DefaultMainActivity) mContext;
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
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".getPageArgs() " + pageName);
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

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID) {
        ft.add(contentViewID, this);
    }

    public static CordovaPageFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        CordovaPageFragment newFragment = new CordovaPageFragment();
        Bundle bundle = new Bundle();
        if (pageName != null) {
            bundle.putString("pageName", pageName);
        }
        if (pageTitle != null) {
            bundle.putString("pageTitle", pageTitle);
        }
        if (pageArgs != null) {
            bundle.putString("pageArgs", pageArgs);
        }
        if (pageOptions != null) {
            bundle.putString("pageOptions", pageOptions);
        }

        newFragment.setArguments(bundle);

        return newFragment;
    }

    public static CordovaPageFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }

}