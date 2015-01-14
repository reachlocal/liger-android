package com.reachlocal.mobile.liger.test;

import android.test.ActivityInstrumentationTestCase2;

import com.reachlocal.mobile.liger.model.AppConfig;
import com.reachlocal.mobile.liger.ui.DrawerFragment;


public class LigerDrawerFragmentWithAppMenuTest extends ActivityInstrumentationTestCase2<TestDefaultMainActivity> {
    TestDefaultMainActivity myTestActivity;

    public LigerDrawerFragmentWithAppMenuTest() {
        super(TestDefaultMainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        AppConfig testConfig = AppConfig.parseAppConfig("{\n" +
                "  \"appFormatVersion\": 6,\n" +
                "  \"rootPage\": {\n" +
                "    \"page\": \"drawer\",\n" +
                "    \"accessibilityLabel\": \"drawer\",\n" +
                "    \"args\": {\n" +
                "       \"page\": \"appMenu\",\n" +
                "       \"accessibilityLabel\": \"menu\",  " +
                "       \"args\": { " +
                "        \"menu\": [\n" +
                "          [\n" +
                "            {\n" +
                "              \"name\": \"Page tests\",\n" +
                "              \"page\": \"navigator\",\n" +
                "              \"accessibilityLabel\": \"Menu1\",\n" +
                "              \"args\": {\n" +
                "                \"title\": \"First Page\",\n" +
                "                \"page\": \"firstPage\",\n" +
                "                \"accessibilityLabel\": \"firstPage\",\n" +
                "                \"args\": {\n" +
                "                  \"hello\": \"world\"\n" +
                "                },\n" +
                "                \"options\": {\n" +
                "                    \"right\":{\"button\":\"search\"}\n" +
                "                }\n" +
                "              }\n" +
                "            }" +
                "          ],[  " +
                "            { \"name\": \"First Page\",\n" +
                "                \"page\": \"firstPage\",\n" +
                "                \"accessibilityLabel\": \"firstPage\",\n" +
                "                \"args\": {\n" +
                "                  \"hello\": \"world\"\n" +
                "                 },\n" +
                "                \"options\": {\n" +
                "                    \"right\":{\"button\":\"search\"}\n" +
                "                }\n" +
                "            }" +
                "          ] " +
                "        ] " +
                "       }}}}");
        AppConfig.setAppConfig(testConfig);
        super.setUp();
        myTestActivity = getActivity();
    }

    public void testPreConditions() {
        assertNotNull(myTestActivity);
        assertNotNull(myTestActivity.getRootPageFragment());
        assertTrue(myTestActivity.getRootPageFragment() instanceof DrawerFragment);
    }

}