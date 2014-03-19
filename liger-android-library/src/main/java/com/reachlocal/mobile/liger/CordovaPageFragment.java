package com.reachlocal.mobile.liger;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.*;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.reachlocal.mobile.liger.model.ToolbarItemSpec;
import com.reachlocal.mobile.liger.utils.CompatUtils;
import com.reachlocal.mobile.liger.widgets.ToolbarLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CordovaWebViewClient;
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

    // Instance State
    private String parentUpdateArgs;
    private String toolbarSpec;
    private String childUpdateArgs;
    private String actionBarTitle;
    private boolean canRefresh;

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
        View view = inflater.inflate(R.layout.page_fragment, container, false);
        mWebView = (FixedCordovaWebView) view.findViewById(R.id.web_view);
        mToolbarLayout = (ToolbarLayout) view.findViewById(R.id.toolbar);

        mWebView.setTag(R.id.web_view_parent_frag, this);
        mWebView.setWebChromeClient(new LoggingChromeClient());
        mWebView.setWebViewClient(getWebClient());
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
        if (isResumed && !isHidden()) {
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
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageFragment.sendJavascript() to " + (mWebView == null ? "(null webView)" : mWebView.getUrl()) + ", js:" + js);
        }
        if (mWebView != null) {
            mWebView.sendJavascript(js);
        }
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

    protected void onPageFinished(String url) {
        if (!isHidden()) {
            updateTitle();
        }
        sendChildArgs();
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

    private WebClient getWebClient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new ICSWebClient();
        }
        return new WebClient();
    }

    class WebClient extends CordovaWebViewClient {

        WebClient() {
            super(activity, mWebView);
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            if (LIGER.LOGGING) {
                Log.d(LIGER.TAG, "PageFragment.onPageFinished() " + pageName + ", " + url);
            }
            isLoaded = true;
            String webTitle = view.getTitle();
            if (StringUtils.isEmpty(actionBarTitle) && !StringUtils.isEmpty(webTitle)) {
                actionBarTitle = webTitle;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CordovaPageFragment.this.onPageFinished(url);
                }
            });
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (LIGER.LOGGING) {
                Log.d(LIGER.TAG, "PageFragment.onLoadResource() " + url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (LIGER.LOGGING) {
                Log.d(LIGER.TAG, String.format("PageFragment.onReceivedError(), errorCode %d, description: %s, failuingUrl: %s ", errorCode, description, failingUrl));
            }
        }


    }

    class ICSWebClient extends WebClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return CompatUtils.icsShouldInterceptRequest(getActivity(), view, url);
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
//        if(activity != null) {
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    sendPageArgs();
//                }
//            });
//        }
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

    public static PageFragment build(String pageName, String pageTitle, String pageArgs) {
        PageFragment newFragment = new CordovaPageFragment();
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

    public static PageFragment build(String pageName, String pageTitle, JSONObject pageArgs) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString());
    }

}