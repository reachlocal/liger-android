package com.reachlocal.mobile.liger.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.reachlocal.mobile.liger.ui.PageFragment;

import java.util.ArrayList;

/**
 * Created by Mark Wagner on 1/7/15.
 */
public class LigerPagerAdapter extends FragmentStatePagerAdapter {

    protected ArrayList<PageFragment> mFragDeck = new ArrayList<PageFragment>();

    public LigerPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
