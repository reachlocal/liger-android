package com.reachlocal.mobile.liger.test;

import android.test.ActivityInstrumentationTestCase2;

import com.reachlocal.mobile.liger.ui.DefaultMainActivity;
import com.reachlocal.mobile.liger.ui.LigerNavigatorFragment;


public class LigerNavigatorFragmentTest extends ActivityInstrumentationTestCase2<com.reachlocal.mobile.liger.test.TestDefaultMainActivity> {
    TestDefaultMainActivity myTestActivity;
    LigerNavigatorFragment myNavigatorFragment;

    public LigerNavigatorFragmentTest() {
        super("com.reachlocal.mobile.liger.test", TestDefaultMainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myTestActivity = getActivity();

    }

    public void testPreConditions() {
        assertNotNull(myTestActivity);

    }


}