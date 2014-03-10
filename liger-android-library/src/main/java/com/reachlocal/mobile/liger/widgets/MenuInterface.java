package com.reachlocal.mobile.liger.widgets;

import com.reachlocal.mobile.liger.model.MenuItemSpec;

import java.util.List;

public interface MenuInterface {
    void setMenuItems(List<MenuItemSpec> majorItems, List<MenuItemSpec> minorItems);

    void setSelectedItem(String link);
}
