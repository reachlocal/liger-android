package com.reachlocal.mobile.liger.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.listeners.PageLifecycleListener;
import com.reachlocal.mobile.liger.utils.ViewUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;


public class LigerNavigatorFragment extends PageFragment {

    View mNavigatorContentFrame;

    private boolean isLoaded;
    private boolean childArgsSent = false;
    protected boolean mCached = true;

    HashMap<String, PageFragment> mFragCache = new HashMap<String, PageFragment>();

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
        if(container != null)
            container.removeAllViews();
        createContentView(inflater, container, savedInstanceState);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerNavigatorFragment.onCreateView() " + pageName);
        }
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction ft = childFragmentManager.beginTransaction();
        for (PageFragment page : mFragDeck) {
            if(page.isAdded()) {
                //ft.remove(page);
            }else{
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
        if(mNavigatorContentFrame != null)
           id = mNavigatorContentFrame.getId();
        mNavigatorContentFrame = inflater.inflate(R.layout.navigator_layout, container, false);
        mNavigatorContentFrame.setId(id);
        if(mNavigatorContentFrame.getId() == -1)
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

        boolean cancelable = !(StringUtils.equalsIgnoreCase(pageName, "signin") || StringUtils.equalsIgnoreCase(pageName, "advertisers"));
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onDetach(){
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
            Log.d(LIGER.TAG, "LigerNavigatorFragment.onDestroyView() " + pageName);
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
            Log.d(LIGER.TAG, "LigerNavigatorFragment.onHiddenChanged() " + pageName + ", " + hidden);
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
            Log.d(LIGER.TAG, "LigerNavigatorFragment openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }
        PageFragment page = null;

//        if (mCached && mFragCache.containsKey(pageName)) {
//            page = mFragCache.get(pageName);
//        } else {
            page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);
//        }

        if (page != null) {
            page.doPageAppear();
            FragmentManager childFragmentManager = getChildFragmentManager();
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            page.addFragments(ft, mNavigatorContentFrame.getId());
            ft.commit();
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
                    "LigerNavigatorFragment openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()) + ", options:"
                            + (options == null ? null : options.toString()));
        }
        PageFragment dialog = LigerFragmentFactory.openPage(pageName, title, args, options);
        if (dialog != null) {
            //mFragDeck.addLast(dialog);
            dialog.show(getActivity().getSupportFragmentManager(), DIALOG_FRAGMENT);
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
        return mFragDeck.getLast();
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
    public void addFragments(FragmentTransaction ft, int containViewId) {
        ft.add(containViewId, this);
    }

    public static LigerNavigatorFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        LigerNavigatorFragment navigator = new LigerNavigatorFragment();
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
                PageFragment newPage = LigerFragmentFactory.openPage(page.getString("page"), page.getString("title"), page.optJSONObject("args"), page.optJSONObject("options"));
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

    public static LigerNavigatorFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }


}