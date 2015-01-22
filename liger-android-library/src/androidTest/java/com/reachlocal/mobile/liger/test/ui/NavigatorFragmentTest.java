package com.reachlocal.mobile.liger.test;

import android.test.ActivityInstrumentationTestCase2;

import com.reachlocal.mobile.liger.model.AppConfig;
import com.reachlocal.mobile.liger.ui.NavigatorFragment;


public class NavigatorFragmentTest extends ActivityInstrumentationTestCase2<com.reachlocal.mobile.liger.test.TestDefaultMainActivity> {
    TestDefaultMainActivity myTestActivity;

    public NavigatorFragmentTest() {
        super(TestDefaultMainActivity.class);

    }

    @Override
    protected void setUp() throws Exception {
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
        super.setUp();
        myTestActivity = getActivity();

    }

    public void testPreConditions() {
        assertNotNull(myTestActivity);
        assertNotNull(myTestActivity.getRootPageFragment());
        assertTrue(myTestActivity.getRootPageFragment() instanceof NavigatorFragment);
    }

// TODO - Create a working backbutton pressed test - issue #59
// https://github.com/reachlocal/liger-android/issues/59
//    public void testBackButtonPressed() {
//        myTestActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
//        assertFalse(myTestActivity.getRootPageFragment().isAdded());
//    }

}