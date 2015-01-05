package com.reachlocal.mobile.liger.test;

import android.test.ActivityInstrumentationTestCase2;
import com.reachlocal.mobile.liger.model.*;
import com.reachlocal.mobile.liger.ui.DefaultMainActivity;
import com.reachlocal.mobile.liger.ui.LigerNavigatorFragment;
import android.view.KeyEvent;


public class LigerNavigatorFragmentTest extends ActivityInstrumentationTestCase2<com.reachlocal.mobile.liger.test.TestDefaultMainActivity> {
    TestDefaultMainActivity myTestActivity;
    LigerNavigatorFragment myNavigatorFragment;

    public LigerNavigatorFragmentTest() {
        super("com.reachlocal.mobile.liger.test", TestDefaultMainActivity.class);
        AppConfig testConfig = AppConfig.parseAppConfig("{\n" +
                "  \"appFormatVersion\": 6,\n" +
                "  \"rootPage\": {\n" +
                "    \"page\": \"navigator\",\n" +
                "    \"accessibilityLabel\": \"Menu1\",\n" +
                "    \"args\": {\n" +
                "                \"title\": \"First Page\",\n" +
                "                \"page\": \"firstPage\",\n" +
                "                \"accessibilityLabel\": \"firstPage\",\n" +
                "                \"args\": {\n" +
                "                  \"hello\": \"world\"\n" +
                "              },\n" +
                "    \"options\": {\n" +
                "        \"right\":{\"button\":\"search\"}\n" +
                "    }\n" +
                "  }" +
                "}}");
        AppConfig.setAppConfig(testConfig);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myTestActivity = getActivity();
        myNavigatorFragment = (LigerNavigatorFragment) myTestActivity.getRootPageFragment();

    }

    public void testPreConditions() {
        assertNotNull(myTestActivity);
        assertNotNull(myNavigatorFragment);
    }

    public void testBackButtonPressed() {
        myTestActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        assertTrue(!myNavigatorFragment.isAdded());
    }

}