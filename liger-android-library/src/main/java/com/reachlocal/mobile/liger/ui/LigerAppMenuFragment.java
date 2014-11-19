package com.reachlocal.mobile.liger.ui;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.model.MenuItemSpec;
import com.reachlocal.mobile.liger.widgets.MenuInterface;
import com.reachlocal.mobile.liger.widgets.MenuItemCell;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark Wagner on 11/6/14.
 */
public class LigerAppMenuFragment extends PageFragment implements MenuInterface {

    String mSelectedItem;
    private List<MenuItemSpec> mMajorMenuItems;
    private List<MenuItemSpec> mMinorMenuItems;

    List<MenuItemCell> mMenuItems = new ArrayList<MenuItemCell>();

    LinearLayout mDrawerContentLayout;


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

    @Override
    public void setMenuItems(List<MenuItemSpec> majorItems, List<MenuItemSpec> minorItems) {
        mMajorMenuItems = majorItems;
        mMinorMenuItems = minorItems;
        if (mDrawerContentLayout != null) {
            addMenuItemCells();
        }
    }

    @Override
    public void setSelectedItem(String link) {
        mSelectedItem = link;
        if (mDrawerContentLayout != null) {
            updateSelectedItem();
        }
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

    private void addMenuItemCells() {
        if (mDrawerContentLayout.getChildCount() != 1) {
            throw new RuntimeException("Menu Drawer initialized with incorrect number of children!");
        }
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        int minorCount = mMinorMenuItems == null ? 0 : mMinorMenuItems.size();
        if (minorCount == 0) {
            mDrawerContentLayout.removeViewAt(0);
        } else {
            for (int i = 0; i < minorCount; i++) {
                MenuItemCell cell = insertCell(inflater, mMinorMenuItems.get(i), i + 1);
                //mDrawerCache.put(cell.getMenuIdString(), cell.
                cell.setChecked(false);
            }
        }
        int majorCount = mMajorMenuItems == null ? 0 : mMajorMenuItems.size();
        for (int i = 0; i < majorCount; i++) {
            MenuItemCell cell = insertCell(inflater, mMajorMenuItems.get(i), i);
            //mDrawerCache.put(cell.getMenuIdString(), cell.
            cell.setChecked(i == 0);
        }
    }

    private void updateSelectedItem() {
        for (MenuItemCell cell : mMenuItems) {
            boolean checked = StringUtils.equalsIgnoreCase(mSelectedItem, cell.getClickAction());
            cell.setChecked(checked);
        }
    }

    public static List<MenuItemSpec> createMenuItems(JSONArray menuArray, boolean major) throws JSONException {
        List<MenuItemSpec> menuList = new ArrayList<MenuItemSpec>();
        int size = menuArray.length();
        for (int i = 0; i < size; i++) {
            menuList.add(MenuItemSpec.createFromJSON(menuArray.getJSONObject(i), major, i));
        }
        return menuList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerContentLayout = (LinearLayout) inflater.inflate(R.layout.drawer_menu, container, false);
        if (mMajorMenuItems != null || mMinorMenuItems != null) {
            addMenuItemCells();
        }
        if (mSelectedItem != null) {
            updateSelectedItem();
        }
        return mDrawerContentLayout;
    }

    @Override
    public String getPageName() {
        return null;
    }

    @Override
    public String getPageTitle() {
        return null;
    }

    @Override
    public void setToolbar(String toolbarSpec) {

    }

    @Override
    public void setChildArgs(String childUpdateArgs) {

    }

    @Override
    public String getPageArgs() {
        return null;
    }

    @Override
    public String getParentUpdateArgs() {
        return null;
    }

    @Override
    public void setParentUpdateArgs(String parentUpdateArgs) {

    }

    @Override
    public void setUserCanRefresh(boolean canRefresh) {

    }

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID) {
        ft.add(contentViewID, this);
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {
        return null;
    }

    @Override
    protected PageFragment getChildPage() {
        //TODO  This should pass to active page displayed
        return null;
    }

    public static LigerAppMenuFragment build(String pageName, String pageTitle, String pageArgs, String pageOptions) {
        LigerAppMenuFragment menu = new LigerAppMenuFragment();
        Bundle bundle = new Bundle();
        JSONArray pages = new JSONArray();
        if (pageName != null) {
            bundle.putString("pageName", pageName);
        }
        if (pageTitle != null) {
            bundle.putString("pageTitle", pageTitle);
        }
        if (pageOptions != null) {
            bundle.putString("pageOptions", pageOptions);
        }

        if (pageArgs != null) {
            bundle.putString("pageArgs", pageArgs);

            JSONObject childPageArgs = null;
            try {
                childPageArgs = new JSONObject(pageArgs);

                JSONArray menuParentArray = childPageArgs.getJSONArray("menu");
                JSONArray majorMenu = menuParentArray.optJSONArray(0);
                if (majorMenu != null) {
                    menu.setMajorMenuItems(menu.createMenuItems(majorMenu, true));
                }
                JSONArray minorMenu = menuParentArray.optJSONArray(1);
                if (minorMenu != null) {
                    menu.setMinorMenuItems(menu.createMenuItems(minorMenu, false));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        menu.setArguments(bundle);

        return menu;
    }

    public static LigerAppMenuFragment build(String pageName, String pageTitle, JSONObject pageArgs, JSONObject pageOptions) {
        return build(pageName, pageTitle, pageArgs == null ? null : pageArgs.toString(), pageOptions == null ? null : pageOptions.toString());
    }
}
