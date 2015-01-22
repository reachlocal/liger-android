package com.reachlocal.mobile.liger.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.reachlocal.mobile.liger.ApplicationState;
import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.factories.FragmentFactory;
import com.reachlocal.mobile.liger.listeners.PageLifecycleListener;
import com.reachlocal.mobile.liger.utils.ViewUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by Mark Wagner on 10/22/14.
 */

public class TabContainerFragment extends PageFragment implements PageLifecycleListener {

    protected PageFragment mTabs;
    protected PageFragment mCurrentTab;
    HashMap<String, PageFragment> mTabCache = new HashMap<String, PageFragment>();
    View mTabsContainer;
    FrameLayout mTabsHolder;
    FrameLayout mTabsContent;
    private JSONObject tabsObject;

    public static TabContainerFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        TabContainerFragment tabsFragment = new TabContainerFragment();
        Bundle bundle = new Bundle();
        if (pageName != null) {
            bundle.putString("pageName", pageName);
        }
        if (pageTitle != null) {
            bundle.putString("pageTitle", pageTitle);
        }
        if (pageArgs != null) {
            bundle.putString("pageArgs", pageArgs);
            try {
                tabsFragment.tabsObject = new JSONObject(pageArgs);
            } catch (JSONException e) {
                throw new RuntimeException("Invalid app.json!", e);
            }

        }
        if (pageOptions != null) {
            bundle.putString("pageOptions", pageOptions);
        }

        tabsFragment.setArguments(bundle);

        return tabsFragment;
    }

    public static TabContainerFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        createContentView(inflater, container, savedInstanceState);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "TabContainerFragment.onCreateView() " + pageName);
        }

        mTabs = FragmentFactory.openPage(tabsObject);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        mTabs.addFragments(ft, mTabsHolder.getId());
        ft.commit();

        mTabs.mLifecycleListeners.add(this);
        return mTabsContainer;
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTabsContainer = inflater.inflate(R.layout.tab_layout, container, false);
        if (mTabsContainer.getId() == -1)
            mTabsContainer.setId(ViewUtil.generateViewId());

        mTabsHolder = new FrameLayout(inflater.getContext());
        if (mTabsHolder.getId() == -1)
            mTabsHolder.setId(ViewUtil.generateViewId());

        mTabsContent = new FrameLayout(inflater.getContext());
        if (mTabsContent.getId() == -1)
            mTabsContent.setId(ViewUtil.generateViewId());

        int dp = (int) getResources().getDimension(R.dimen.tab_height);

        RelativeLayout.LayoutParams tabsParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, dp);
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        containerParams.addRule(RelativeLayout.BELOW, mTabsHolder.getId());
        ((RelativeLayout) mTabsContainer).addView(mTabsHolder, tabsParams);
        ((RelativeLayout) mTabsContainer).addView(mTabsContent, containerParams);

        return mTabsContainer;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean hasContentFrame() {
        return true;
    }

    @Override
    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "TabContainerFragment openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }

        Boolean cached = true;
        PageFragment page;
        String reuseIdentifier = null;

        if (pageOptions != null) {
            reuseIdentifier = pageOptions.optString("reuseIdentifier", null);
            cached = pageOptions.optBoolean("cached", true);
        }

        if (reuseIdentifier != null && (mTabCache.containsKey(reuseIdentifier) && pageOptions.optBoolean("cached", true) == true)) {
            page = mTabCache.get(reuseIdentifier);
        } else {
            page = FragmentFactory.openPage(pageName, title, pageArgs, pageOptions);
            if (cached && reuseIdentifier != null) {
                mTabCache.put(reuseIdentifier, page);
            }
        }

        if (page != null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

            if (mCurrentTab != null) {
                ft.detach(mCurrentTab);
            }

            page.doPageAppear();

            if (page.isDetached() || page == mCurrentTab) {
                ft.attach(page);
            } else {
                page.addFragments(ft, mTabsContent.getId());
            }
            ft.commit();
            mCurrentTab = page;
            mCurrentTab.mContainer = this;
        }
    }

    @Override
    public void openDialog(String pageName, String title, JSONObject args, JSONObject options) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG,
                    "TabContainerFragment openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()) + ", options:"
                            + (options == null ? null : options.toString()));
        }
        PageFragment dialog = FragmentFactory.openPage(pageName, title, args, options);
        if (dialog != null) {
            mFragDeck.addLast(dialog);
            dialog.show(getActivity().getSupportFragmentManager(), DIALOG_FRAGMENT);
        }
    }

    @Override
    public String getPageName() {
        return null;
    }

    @Override
    public String getPageTitle() {
        return mFragDeck.getLast().getPageTitle();
    }

    @Override
    public void setToolbar(String toolbarSpec) {
    }

    @Override
    public void setChildArgs(String childUpdateArgs) {
        mCurrentTab.setChildArgs(childUpdateArgs);
    }

    @Override
    public void notificationArrived(JSONObject notificationPayload, ApplicationState applicationState) {
        mTabs.notificationArrived(notificationPayload, applicationState);
    }

    @Override
    public String getPageArgs() {
        if (mCurrentTab != null) {
            return mCurrentTab.getPageArgs();
        } else {
            return null;
        }
    }

    @Override
    public String getParentUpdateArgs() {
        return mCurrentTab.getParentUpdateArgs();
    }

    @Override
    public void setParentUpdateArgs(String parentUpdateArgs) {
        mCurrentTab.setParentUpdateArgs(parentUpdateArgs);
    }

    @Override
    public void sendJavascript(String js) {
        if (mCurrentTab != null) {
            mCurrentTab.sendJavascript(js);
        }
    }

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID) {
        ft.add(contentViewID, this);
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {
        mCurrentTab.closeLastPage(closePage, closeTo);
        return null;
    }

    @Override
    protected PageFragment getChildPage() {
        return mTabs;
    }


    @Override
    public void onPageClosed(PageFragment page) {

    }

    @Override
    public void onHiddenChanged(PageFragment page, boolean hidden) {

    }

    @Override
    public void onPageFinished(PageFragment page) {
        if (mTokenHolder != null) {
            page.pushNotificationTokenUpdated(mTokenHolder.registrationId, mTokenHolder.errorMessage);
        }
    }
}
