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
import com.reachlocal.mobile.liger.model.MenuItemSpec;
import com.reachlocal.mobile.liger.widgets.MenuInterface;
import com.reachlocal.mobile.liger.widgets.MenuItemCell;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mark Wagner on 10/22/14.
 */

public class LigerDrawerFragment extends PageFragment implements MenuInterface {

    LinearLayout mDrawerContentLayout;

    List<MenuItemSpec> mMajorItems;
    List<MenuItemSpec> mMinorItems;
    String mSelectedItem;

    HashMap<String, PageFragment> mDrawerCache = new HashMap<String, PageFragment>();

    private List<MenuItemSpec> mMajorMenuItems;
    private List<MenuItemSpec> mMinorMenuItems;

    List<MenuItemCell> mMenuItems = new ArrayList<MenuItemCell>();


    public List<MenuItemSpec> getMajorMenuItems() {
        return mMajorMenuItems;
    }

    public void setMajorMenuItems(List<MenuItemSpec> majorMenuItems) {
        mMajorMenuItems = majorMenuItems;
    }

    public List<MenuItemSpec> getMinorMenuItems() {
        return mMinorMenuItems;
    }

    public void setMinorMenuItems(List<MenuItemSpec> minorMenuItems) {
        mMinorMenuItems = minorMenuItems;
    }


    private void setupMenu() {
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        ((DefaultMainActivity)mContext).menuDrawer = (DrawerLayout) mContext.findViewById(R.id.drawer_layout);
        setMenuItems(getMajorMenuItems(), getMinorMenuItems());

        ((DefaultMainActivity)mContext).menuToggle = new ActionBarDrawerToggle(mContext, /* host Activity */
                ((DefaultMainActivity)mContext).menuDrawer, /* DrawerLayout object */
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
        ((DefaultMainActivity)mContext).menuToggle.setDrawerIndicatorEnabled(true);

        ((DefaultMainActivity)mContext).menuDrawer.setDrawerListener(((DefaultMainActivity)mContext).menuToggle);
    }


    @Override
    public void setMenuItems(List<MenuItemSpec> majorItems, List<MenuItemSpec> minorItems) {
        mMajorItems = majorItems;
        mMinorItems = minorItems;
        if(mDrawerContentLayout != null) {
            addMenuItemCells();
        }
    }

    private void addMenuItemCells() {
        if(mDrawerContentLayout.getChildCount() != 1) {
            throw new RuntimeException("Menu Drawer initialized with incorrect number of children!");
        }
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        int minorCount = mMinorItems == null ? 0 : mMinorItems.size();
        if(minorCount == 0) {
            mDrawerContentLayout.removeViewAt(0);
        } else {
            for(int i = 0; i < minorCount; i++) {
                MenuItemCell cell = insertCell(inflater, mMinorItems.get(i), i + 1);
                //mDrawerCache.put(cell.getMenuIdString(), cell.
                cell.setChecked(false);
            }
        }
        int majorCount = mMajorItems == null ? 0 : mMajorItems.size();
        for(int i = 0; i < majorCount; i++) {
            MenuItemCell cell = insertCell(inflater, mMajorItems.get(i), i);
            //mDrawerCache.put(cell.getMenuIdString(), cell.
            cell.setChecked(i == 0);
        }
    }

    @Override
    public void setSelectedItem(String link) {
        mSelectedItem = link;
        if(mDrawerContentLayout != null) {
            updateSelectedItem();
        }
    }

    private void updateSelectedItem() {
        for (MenuItemCell cell : mMenuItems) {
            boolean checked = StringUtils.equalsIgnoreCase(mSelectedItem, cell.getClickAction());
            cell.setChecked(checked);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerContentLayout = (LinearLayout) inflater.inflate(R.layout.drawer_menu, container, false);
        if(mMajorItems != null || mMinorItems != null) {
            addMenuItemCells();
        }
        if(mSelectedItem != null) {
            updateSelectedItem();
        }
        setupMenu();
        return mDrawerContentLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private MenuItemCell insertCell(LayoutInflater inflater, MenuItemSpec itemSpec, int insertPosition) {
        MenuItemCell cell = (MenuItemCell) inflater.inflate(R.layout.menu_item_cell, mDrawerContentLayout, false);
        cell.setDialog(itemSpec.isDialog());
        cell.setClickAction(itemSpec.getPage());
        cell.setIconGlyph(itemSpec.getIconText());
        cell.setItemTitle(itemSpec.getName());
        cell.setItemSubtitle(itemSpec.getDetail());
        cell.setItemArgs(itemSpec.getArgs());
        cell.setItemOptions(itemSpec.getOptions());
        cell.setMajor(itemSpec.isMajor());
        cell.setMenuIdString(itemSpec.getMenuIdString());
        cell.updateText();
        mDrawerContentLayout.addView(cell, insertPosition);
        mMenuItems.add(cell);
        return cell;
    }

    @Override
    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerDrawerFragment openPage() pageName:" + pageName + ", args:" + pageArgs+ ", options:" + pageOptions);
        }
        PageFragment page = null;

        //TODO - Frag Cache
//        if(mFragCache.containsKey(pageName)){
//            page = mFragCache.get(pageName);
//        }else{
//            page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);
//        }

        page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);

        if(page != null) {
            page.doPageAppear();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            if(mFragDeck.size() > 0){
                PageFragment previousPage = mFragDeck.getLast();
                ft.remove(previousPage);
                mFragDeck.removeLast();
            }
            page.addFragments(ft, R.id.content_frame );
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
        PageFragment dialog =  LigerFragmentFactory.openPage(pageName, title, args, options);
        if(dialog != null) {
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
        if(mFragDeck.size() > 0) {
            return mFragDeck.getLast().getPageArgs();
        }else{
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

    public static List<MenuItemSpec> createMenuItems(JSONArray menuArray, boolean major) throws JSONException {
        List<MenuItemSpec> menuList = new ArrayList<MenuItemSpec>();
        int size = menuArray.length();
        for (int i = 0; i < size; i++) {
            menuList.add(MenuItemSpec.createFromJSON(menuArray.getJSONObject(i), major, i));
        }
        return menuList;
    }

    public static LigerDrawerFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        LigerDrawerFragment newFragment = new LigerDrawerFragment();
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
                JSONObject childPage = new JSONObject(pageArgs);

                if (childPage.getString("page").equalsIgnoreCase("appMenu")){
                    JSONObject childPageArgs = childPage.getJSONObject("args");
                    JSONArray menuParentArray = childPageArgs.getJSONArray("menu");
                    JSONArray majorMenu = menuParentArray.optJSONArray(0);
                    if (majorMenu != null) {
                        newFragment.setMajorMenuItems(newFragment.createMenuItems(majorMenu, true));
                    }
                    JSONArray minorMenu = menuParentArray.optJSONArray(1);
                    if (minorMenu != null) {
                        newFragment.setMinorMenuItems(newFragment.createMenuItems(minorMenu, false));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException("Invalid app.json!", e);
            }

        }
        if (pageOptions != null) {
            bundle.putString("pageOptions", pageOptions);
        }

        newFragment.setArguments(bundle);

        return newFragment;
    }

    public static LigerDrawerFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID){
        ft.add(R.id.drawer_menu, this);
        MenuItemSpec firstMenuItem = getMajorMenuItems().get(0);
        PageFragment page = LigerFragmentFactory.openPage(firstMenuItem.getPage(), firstMenuItem.getName(), firstMenuItem.getArgs(), firstMenuItem.getOptions());
        if(page != null) {
            mFragDeck.addLast(page);
            page.mContainer = this;
            page.addFragments(ft, contentViewID);
        }
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {

        PageFragment parentPage = null;

        if(mFragDeck.size() > 0) {
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
                if(lastPage instanceof LigerNavigatorFragment){
                    lastPage.closeLastPage(closePage, closeTo);
                }else {
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
        if(mFragDeck.size() == 0 || (mFragDeck.size() == 1 && mFragDeck.getLast().isDetached())){
            FragmentTransaction ft = mContext.getSupportFragmentManager().beginTransaction();
            ft.remove(this);
            ft.commit();
        }
        return parentPage == null ? null : parentPage.getPageName();
    }
}
