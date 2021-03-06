package com.reachlocal.mobile.liger.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.reachlocal.mobile.liger.ApplicationState;
import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.factories.FragmentFactory;
import com.reachlocal.mobile.liger.listeners.PageLifecycleListener;
import com.reachlocal.mobile.liger.utils.ViewUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;


public class NavigatorFragment extends PageFragment {

    protected boolean mCached = true;
    View mNavigatorContentFrame;
    HashMap<String, PageFragment> mFragCache = new HashMap<String, PageFragment>();
    // Fragment arguments
    String pageArgs;
    String pageOptions;
    boolean isDialog;
    private boolean isLoaded;
    private boolean childArgsSent = false;
    // Instance State
    private String parentUpdateArgs;
    private String toolbarSpec;
    private String childUpdateArgs;
    private String actionBarTitle;
    private boolean canRefresh;

    public static NavigatorFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        NavigatorFragment navigator = new NavigatorFragment();
        Bundle bundle = new Bundle();
        JSONArray pages = new JSONArray();
        if (pageName != null) {
            bundle.putString("pageName", pageName);
        }
        if (pageTitle != null) {
            bundle.putString("pageTitle", pageTitle);
        }
        try {
            if (pageOptions != null) {
                bundle.putString("pageOptions", pageOptions);
                JSONObject jsonPageOptions = new JSONObject(pageOptions);
                if (jsonPageOptions.has("cached")) {
                    navigator.mCached = jsonPageOptions.getBoolean("cached");
                } else {
                    navigator.mCached = true;
                }
            }

            if (pageArgs != null) {
                bundle.putString("pageArgs", pageArgs);

                JSONObject jsonPageArgs = new JSONObject(pageArgs);

                if (jsonPageArgs.has("pages")) {
                    //Contains an Array of pages load them onto the stack in order.
                    pages = jsonPageArgs.getJSONArray("pages");
                } else {
                    // Navigator with one page
                    pages.put(jsonPageArgs);
                }
            }
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i);
                PageFragment newPage = FragmentFactory.openPage(page.getString("page"), page.getString("title"), page.optJSONObject("args"), page.optJSONObject("options"));
                if (newPage != null) {
                    if (navigator.mCached) {
                        navigator.mFragCache.put(page.getString("page"), newPage);
                    }
                    navigator.mFragDeck.addLast(newPage);
                    newPage.mContainer = navigator;
                }
            }

        } catch (JSONException e) {
            throw new RuntimeException("Invalid app.json!", e);
        }

        navigator.setArguments(bundle);

        return navigator;
    }

    public static NavigatorFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }

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
            Log.d(LIGER.TAG, "PageFragment.onCreate() " + pageName);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("parentUpdateArgs", parentUpdateArgs);
        outState.putString("toolbarSpec", toolbarSpec);
        outState.putString("childUpdateArgs", childUpdateArgs);
        outState.putString("actionBarTitle", actionBarTitle);
        outState.putBoolean("canRefresh", canRefresh);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container != null)
            container.removeAllViews();
        createContentView(inflater, container, savedInstanceState);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "NavigatorFragment.onCreateView() " + pageName);
        }
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction ft = childFragmentManager.beginTransaction();
        for (PageFragment page : mFragDeck) {
            if (page.isAdded()) {
                //ft.remove(page);
            } else {
                page.addFragments(ft, mNavigatorContentFrame.getId());
            }
            page.doPageAppear();
            page.mContainer = this;
        }
        ft.commit();
        return mNavigatorContentFrame;
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int id = -1;
        if (mNavigatorContentFrame != null)
            id = mNavigatorContentFrame.getId();
        mNavigatorContentFrame = inflater.inflate(R.layout.navigator_layout, container, false);
        mNavigatorContentFrame.setId(id);
        if (mNavigatorContentFrame.getId() == -1)
            mNavigatorContentFrame.setId(ViewUtil.generateViewId());
        return mNavigatorContentFrame;
    }

    @Override
    public Dialog onCreateDialog(Bundle inState) {
        final Dialog dialog = new Dialog(getActivity(), R.style.AppDialogNoFrame);
        isDialog = true;

        View contentView = createContentView(LayoutInflater.from(getActivity()), null, inState);

        Window window = dialog.getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        boolean cancelable = pageName == null || !(pageName.equalsIgnoreCase("signin") || pageName.equalsIgnoreCase("advertisers"));
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "NavigatorFragment.onDestroyView() " + pageName);
        }

    }

    private void sendChildArgs() {
        //TODO  - What does this do?
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) {
            sendChildArgs();
            doPageAppear();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "NavigatorFragment.onHiddenChanged() " + pageName + ", " + hidden);
        }
        if (!hidden) {
            sendChildArgs();
            doPageAppear();
        }
        for (PageLifecycleListener listener : mLifecycleListeners) {
            listener.onHiddenChanged(this, hidden);
        }
    }

    @Override
    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "NavigatorFragment openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }
        PageFragment page = null;

//        if (mCached && mFragCache.containsKey(pageName)) {
//            page = mFragCache.get(pageName);
//        } else {
        page = FragmentFactory.openPage(pageName, title, pageArgs, pageOptions);
//        }

        if (page != null) {
            page.doPageAppear();
            FragmentManager childFragmentManager = getChildFragmentManager();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            page.addFragments(ft, mNavigatorContentFrame.getId());
            ft.commitAllowingStateLoss();
            mFragDeck.addLast(page);
            if (mCached) {
                mFragCache.put(pageName, page);
            }
            page.mContainer = this;
        }
    }

    @Override
    public void openDialog(String pageName, String title, JSONObject args, JSONObject options) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG,
                    "NavigatorFragment openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()) + ", options:"
                            + (options == null ? null : options.toString()));
        }
        PageFragment dialog = FragmentFactory.openPage(pageName, title, args, options);
        if (dialog != null) {
            try{
                dialog.show(getChildFragmentManager(), DIALOG_FRAGMENT);
            }catch (IllegalStateException e) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.add(dialog, DIALOG_FRAGMENT);
                ft.commitAllowingStateLoss();
            }
        }
    }

    @Override
    public String getPageName() {
        return pageName;
    }

    @Override
    public String getPageTitle() {
        return mFragDeck.getLast().getPageTitle();
    }

    @Override
    public void setToolbar(String toolbarSpec) {

    }

    @Override
    public void doPageClosed() {
        super.doPageClosed();
        for (PageLifecycleListener listener : mLifecycleListeners) {
            listener.onPageClosed(this);
        }
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {
        PageFragment parentPage = null;
        if (mFragDeck.size() > 0) {
            PageFragment lastPage = mFragDeck.getLast();
            if (closeTo != null && !closeTo.isEmpty()) {
                Iterator<PageFragment> it = mFragDeck.descendingIterator();
                while (it.hasNext()) {
                    PageFragment candidate = it.next();
                    if (closeTo != null && closeTo.equals(candidate.getPageName())) {
                        parentPage = candidate;
                        break;
                    }
                }
            }
            if (closePage == null || closePage == lastPage) {
                FragmentManager childFragmentManager = getChildFragmentManager();
                if (childFragmentManager != null) {
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
                    ft.commitAllowingStateLoss();
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
        return mFragDeck.getLast();
    }

    @Override
    public void setChildArgs(String childUpdateArgs) {
        mFragDeck.getLast().setChildArgs(childUpdateArgs);
    }

    @Override
    public void notificationArrived(JSONObject notificationPayload, ApplicationState applicationState) {
        if (mFragDeck.size() > 0) {
            mFragDeck.getLast().notificationArrived(notificationPayload, applicationState);
        }
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

    @Override
    public void addFragments(FragmentTransaction ft, int containViewId) {
        ft.add(containViewId, this);
    }

    private class DialogKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (pageName != null && pageName.equalsIgnoreCase("signin")) {
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


}