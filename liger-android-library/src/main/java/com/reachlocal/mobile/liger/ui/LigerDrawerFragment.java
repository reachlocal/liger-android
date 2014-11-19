package com.reachlocal.mobile.liger.ui;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

public class LigerDrawerFragment extends PageFragment implements PageLifecycleListener {

    private JSONObject drawerObject;
    protected PageFragment mDrawer;

    HashMap<String, PageFragment> mDrawerCache = new HashMap<String, PageFragment>();

    LinearLayout mDrawerContentLayout;

    View mDrawerContentFrame;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        createContentView(inflater, container, savedInstanceState);
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerDrawerFragment.onCreateView() " + pageName);
        }

        mDrawer = LigerFragmentFactory.openPage(drawerObject);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        mDrawer.addFragments(ft, mDrawerContentFrame.getId());
        ft.commit();
        setupMenu();
        mDrawer.mLifecycleListeners.add(this);
        return mDrawerContentFrame;
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerContentFrame = inflater.inflate(R.layout.navigator_layout, container, false);
        mDrawerContentFrame.setId(ViewUtil.generateViewId());
        return mDrawerContentFrame;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setupMenu() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        ((DefaultMainActivity) mContext).menuDrawer = (DrawerLayout) mContext.findViewById(R.id.drawer_layout);
        //setMenuItems(getMajorMenuItems(), getMinorMenuItems());

        ((DefaultMainActivity) mContext).menuToggle = new ActionBarDrawerToggle(mContext, /* host Activity */
                ((DefaultMainActivity) mContext).menuDrawer, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.menu_open_desc, /* "open drawer" description for accessibility */
                R.string.menu_close_desc /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActivity().supportInvalidateOptionsMenu(); // creates call to
            }

            public void onDrawerOpened(View drawerView) {
                getActivity().supportInvalidateOptionsMenu(); // creates call to
            }
        };
        ((DefaultMainActivity) mContext).menuToggle.setDrawerIndicatorEnabled(true);

        ((DefaultMainActivity) mContext).menuDrawer.setDrawerListener(((DefaultMainActivity) mContext).menuToggle);
    }


    @Override
    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerDrawerFragment openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }
        PageFragment page = null;

        //TODO - Frag Cache
//        if(mFragCache.containsKey(pageName)){
//            page = mFragCache.get(pageName);
//        }else{
//            page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);
//        }

        page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);

        if (page != null) {
            page.doPageAppear();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            if (mFragDeck.size() > 0) {
                PageFragment previousPage = mFragDeck.getLast();
                ft.remove(previousPage);
                mFragDeck.removeLast();
            }
            page.addFragments(ft, R.id.content_frame);
            ft.commit();
            mFragDeck.addLast(page);
            //TODO mFragCache.put(pageName, page);

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
    public void setUserCanRefresh(boolean canRefresh) {

    }


    public static LigerDrawerFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        LigerDrawerFragment drawerFragment = new LigerDrawerFragment();
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
                drawerFragment.drawerObject = new JSONObject(pageArgs);
            } catch (JSONException e) {
                throw new RuntimeException("Invalid app.json!", e);
            }

        }
        if (pageOptions != null) {
            bundle.putString("pageOptions", pageOptions);
        }

        drawerFragment.setArguments(bundle);

        return drawerFragment;
    }

    public static LigerDrawerFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID) {
        ft.add(R.id.drawer_menu, this);
        //mDrawer = LigerFragmentFactory.openPage(drawerObject);
        //ft = getChildFragmentManager().beginTransaction();
        //mDrawer.addFragments(ft, R.id.drawer_menu );

//        MenuItemSpec firstMenuItem = getMajorMenuItems().get(0);
//        PageFragment page = LigerFragmentFactory.openPage(firstMenuItem.getPage(), firstMenuItem.getName(), firstMenuItem.getArgs(), firstMenuItem.getOptions());
//        if (page != null) {
//            mFragDeck.addLast(page);
//            page.mContainer = this;
//            page.addFragments(ft, contentViewID);
//        }

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
                if (lastPage instanceof LigerNavigatorFragment) {
                    lastPage.closeLastPage(closePage, closeTo);
                } else {
                    FragmentTransaction ft = mContext.getSupportFragmentManager().beginTransaction();
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
                        ((DefaultMainActivity) mContext).setActionBarTitle(parentPage.getPageTitle());
                    }
                    ft.commit();
                }

                logStack("closeLastPage");
            } else {
                lastPage.closeLastPage(closePage, closeTo);
            }


        }
        if (mFragDeck.size() == 0 || (mFragDeck.size() == 1 && mFragDeck.getLast().isDetached())) {
            FragmentTransaction ft = mContext.getSupportFragmentManager().beginTransaction();
            ft.remove(this);
            ft.commit();
        }
        return parentPage == null ? null : parentPage.getPageName();
    }

    @Override
    protected PageFragment getChildPage() {
        return mDrawer;
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
