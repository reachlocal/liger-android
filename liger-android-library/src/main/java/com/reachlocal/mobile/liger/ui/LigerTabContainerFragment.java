package com.reachlocal.mobile.liger.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.listeners.PageLifecycleListener;
import com.reachlocal.mobile.liger.utils.ViewUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by Mark Wagner on 10/22/14.
 */

public class LigerTabContainerFragment extends PageFragment implements PageLifecycleListener {

    private JSONObject tabsObject;
    protected PageFragment mTabs;
    protected PageFragment mTabContent;

    HashMap<String, PageFragment> mTabCache = new HashMap<String, PageFragment>();

    View mTabsContainer;
    FrameLayout mTabsHolder;
    FrameLayout mTabsContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        createContentView(inflater, container, savedInstanceState);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerDrawerFragment.onCreateView() " + pageName);
        }

        mTabs = LigerFragmentFactory.openPage(tabsObject);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        mTabs.addFragments(ft, mTabsHolder.getId());
        ft.commit();

        mTabs.mLifecycleListeners.add(this);
        return mTabsContainer;
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTabsContainer = inflater.inflate(R.layout.tab_layout, container, false);        
        if(mTabsContainer.getId() == -1)
            mTabsContainer.setId(ViewUtil.generateViewId());

        mTabsHolder = new FrameLayout(inflater.getContext());
        if(mTabsHolder.getId() == -1)
            mTabsHolder.setId(ViewUtil.generateViewId());
        
        mTabsContent = new FrameLayout(inflater.getContext());
        if(mTabsContent.getId() == -1)
            mTabsContent.setId(ViewUtil.generateViewId());

        RelativeLayout.LayoutParams tabsParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 200);
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
            Log.d(LIGER.TAG, "LigerDrawerFragment openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }
        PageFragment page;
        String reuseIdentifier = pageOptions.optString("reuseIdentifier", null);

        if(reuseIdentifier != null && mTabCache.containsKey(reuseIdentifier)){
            page = mTabCache.get(reuseIdentifier);
        }else{
            page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);
            Boolean cached = pageOptions.optBoolean("cached", true);
            if(cached && reuseIdentifier != null){
                mTabCache.put(reuseIdentifier, page);
            }
        }

        if (page != null) {
            page.doPageAppear();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            if (mFragDeck.size() > 0) {
                PageFragment previousPage = mFragDeck.getLast();
                if(previousPage == page)
                    return;
                //ft.remove(previousPage);
                ft.detach(previousPage);
                mFragDeck.removeLast();
            }
            if(page.isDetached()) {
                ft.attach(page);
            }else{
                page.addFragments(ft, mTabsContent.getId());
            }
            ft.commit();
            mFragDeck.addLast(page);
            mTabCache.put(pageName, page);

            page.mContainer = this;
        }
    }

    @Override
    public void openDialog(String pageName, String title, JSONObject args, JSONObject options) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG,
                    "LigerDrawerFragment openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()) + ", options:"
                            + (options == null ? null : options.toString()));
        }
        PageFragment dialog = LigerFragmentFactory.openPage(pageName, title, args, options);
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
        mFragDeck.getLast().setChildArgs(childUpdateArgs);
    }

    @Override
    public String getPageArgs() {
        if (mFragDeck.size() > 0) {
            return mFragDeck.getLast().getPageArgs();
        } else {
            return null;
        }
    }

    @Override
    public String getParentUpdateArgs() {
        return mFragDeck.getLast().getParentUpdateArgs();
    }

    @Override
    public void setParentUpdateArgs(String parentUpdateArgs) {
        mFragDeck.getLast().setParentUpdateArgs(parentUpdateArgs);
    }

    @Override
    public void sendJavascript(String js) {
        if (mFragDeck.size() > 0) {
            PageFragment lastPage = mFragDeck.getLast();
            lastPage.sendJavascript(js);
        }
    }

    public static LigerTabContainerFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        LigerTabContainerFragment tabsFragment = new LigerTabContainerFragment();
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

    public static LigerTabContainerFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID) {
        ft.add(contentViewID, this);
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {

        PageFragment parentPage = null;
        if (mFragDeck.size() > 0) {
            PageFragment lastPage = mFragDeck.getLast();
            if (!StringUtils.isEmpty(closeTo)) {
                Iterator<PageFragment> it = mFragDeck.descendingIterator();
                while (it.hasNext()) {
                    PageFragment candidate = it.next();
                    if (StringUtils.equals(closeTo, candidate.getPageName())) {
                        parentPage = candidate;
                        break;
                    }
                }
            }
            if (closePage == null || closePage == lastPage) {
                FragmentManager childFragmentManager = getChildFragmentManager();
                if(childFragmentManager != null) {
                    FragmentTransaction ft = childFragmentManager.beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    mFragDeck.removeLast();
                    lastPage.doPageClosed();
                    ft.remove(lastPage);
                    if (parentPage == null) {
                        if (mFragDeck.size() > 0) {
                            parentPage = mFragDeck.getLast();
                        }
                    } else {
                        popTo(ft, parentPage);

                    }
                    String parentUpdateArgs = lastPage.getParentUpdateArgs();
                    if (parentPage != null) {
                        parentPage.setChildArgs(parentUpdateArgs);
                        parentPage.doPageAppear();
                        ((DefaultMainActivity) getActivity()).setActionBarTitle(parentPage.getPageTitle());
                    }
                    ft.commit();
                }

                logStack("closeLastPage");
            }
        }
        if (mFragDeck.size() == 0) {
            FragmentTransaction ft = mContext.getSupportFragmentManager().beginTransaction();
            ft.remove(this);
            ft.commit();
        }
        return parentPage == null ? null : parentPage.getPageName();
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