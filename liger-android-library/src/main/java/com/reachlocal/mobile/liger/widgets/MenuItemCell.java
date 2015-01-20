package com.reachlocal.mobile.liger.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.ui.DefaultMainActivity;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class MenuItemCell extends LinearLayout implements Checkable {

    public static final int MINOR = 0;
    public static final int MAJOR = 1;
    public static final int DISPLAY_PAGE = 0;
    public static final int DISPLAY_DIALOG = 1;
    CheckedTextView menuIconGlyph;
    CheckedTextView menuItemTitle;
    CheckedTextView menuItemSubtitle;
    private boolean checked = false;
    private String iconGlyph;
    private String clickAction;
    private String itemTitle;
    private String itemSubtitle;
    private boolean mMajor;
    private boolean mDialog;
    private JSONObject itemArgs;
    private JSONObject itemOptions;
    private String mMenuIdString;

    public MenuItemCell(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuItemCell, 0, 0);

        iconGlyph = a.getString(R.styleable.MenuItemCell_iconGlyph);
        clickAction = a.getString(R.styleable.MenuItemCell_action);
        mMajor = a.getInt(R.styleable.MenuItemCell_itemType, MINOR) == MAJOR;
        itemTitle = a.getString(R.styleable.MenuItemCell_android_text);
        itemSubtitle = a.getString(R.styleable.MenuItemCell_subtitle);
        checked = a.getBoolean(R.styleable.MenuItemCell_android_checked, false);
        mDialog = a.getInt(R.styleable.MenuItemCell_displayType, DISPLAY_PAGE) == DISPLAY_DIALOG;

        a.recycle();

        setClickable(true);

        LayoutInflater.from(context).inflate(R.layout.menu_item_content, this);
        menuIconGlyph = (CheckedTextView) findViewById(R.id.menu_icon_glyph);
        menuItemTitle = (CheckedTextView) findViewById(R.id.menu_item_title);
        menuItemSubtitle = (CheckedTextView) findViewById(R.id.menu_item_subtitle);
        afterViewsInjected();
    }

    public MenuItemCell(Context context) {
        super(context);
    }

    @Override
    public boolean performClick() {
        DefaultMainActivity webActivity = (DefaultMainActivity) getContext();
        if (!mDialog) {
            webActivity.openPage(clickAction, itemTitle, itemArgs, itemOptions);
        } else {
            webActivity.openDialog(clickAction, itemTitle, itemArgs, itemOptions);
        }
        return true;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            updateChecked();

        }
    }

    @Override
    public void toggle() {
        checked = !checked;
        updateChecked();

    }

    private void afterViewsInjected() {
        updateStyles();
        updateText();
        updateChecked();
    }

    private void updateStyles() {
        if (!isInEditMode()) {
            if (!mMajor) {
                menuItemTitle.setTextAppearance(getContext(), R.style.menuTextMinor);
            } else {
                menuItemTitle.setTextAppearance(getContext(), R.style.menuTextMajor);
            }

        }
    }

    public void updateText() {
        menuIconGlyph.setText(iconGlyph);
        menuItemTitle.setText(itemTitle);
        menuItemSubtitle.setText(itemSubtitle);
        menuItemSubtitle.setVisibility(StringUtils.isEmpty(itemSubtitle) ? GONE : VISIBLE);
    }

    private void updateChecked() {
        menuIconGlyph.setChecked(checked);
        menuItemTitle.setChecked(checked);
        menuItemSubtitle.setChecked(checked);
    }

    public String getClickAction() {
        return clickAction;
    }

    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    public String getSubtitle() {
        return itemSubtitle;
    }

    public void setSubtitle(String itemSubtitle) {
        this.itemSubtitle = itemSubtitle;
    }

    public String getIconGlyph() {
        return iconGlyph;
    }

    public void setIconGlyph(String iconGlyph) {
        this.iconGlyph = iconGlyph;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemSubtitle() {
        return itemSubtitle;
    }

    public void setItemSubtitle(String itemSubtitle) {
        this.itemSubtitle = itemSubtitle;
    }

    public void setItemArgs(JSONObject args) {
        this.itemArgs = args;
    }

    public void setItemOptions(JSONObject options) {
        this.itemOptions = options;
    }

    public boolean isMajor() {
        return mMajor;
    }

    public void setMajor(boolean major) {
        mMajor = major;
    }

    public boolean isDialog() {
        return mDialog;
    }

    public void setDialog(boolean dialog) {
        mDialog = dialog;
    }

    public String getMenuIdString() {
        return mMenuIdString;
    }

    public void setMenuIdString(String menuIdString) {
        this.mMenuIdString = menuIdString;
    }
}
