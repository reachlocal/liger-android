package com.reachlocal.mobile.liger.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.reachlocal.mobile.liger.PageFragment;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.model.MenuItemSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultMenuFragment extends PageFragment implements MenuInterface {

    LinearLayout mDrawerContentLayout;

    List<MenuItemSpec> mMajorItems;
    List<MenuItemSpec> mMinorItems;
    String mSelectedItem;

    List<MenuItemCell> mMenuItems = new ArrayList<MenuItemCell>();

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
                cell.setChecked(false);
            }
        }
        int majorCount = mMajorItems == null ? 0 : mMajorItems.size();
        for(int i = 0; i < majorCount; i++) {
            MenuItemCell cell = insertCell(inflater, mMajorItems.get(i), i);
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
        return mDrawerContentLayout;
    }

    private MenuItemCell insertCell(LayoutInflater inflater, MenuItemSpec itemSpec, int insertPosition) {
        MenuItemCell cell = (MenuItemCell) inflater.inflate(R.layout.menu_item_cell, mDrawerContentLayout, false);
        cell.setDialog(itemSpec.isDialog());
        cell.setClickAction(itemSpec.getPage());
        cell.setIconGlyph(itemSpec.getIconText());
        cell.setItemTitle(itemSpec.getName());
        cell.setItemSubtitle(itemSpec.getDetail());
        cell.setMajor(itemSpec.isMajor());
        cell.updateText();
        mDrawerContentLayout.addView(cell, insertPosition);
        mMenuItems.add(cell);
        return cell;
    }

    @Override
    public String getPageName() {
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

}
