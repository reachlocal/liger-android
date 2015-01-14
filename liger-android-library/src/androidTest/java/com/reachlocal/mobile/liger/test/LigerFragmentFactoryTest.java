package com.reachlocal.mobile.liger.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.ui.CordovaPageFragment;
import com.reachlocal.mobile.liger.ui.LigerAppMenuFragment;
import com.reachlocal.mobile.liger.ui.DrawerFragment;
import com.reachlocal.mobile.liger.ui.NavigatorFragment;
import com.reachlocal.mobile.liger.ui.PageFragment;

import org.json.JSONObject;


public class LigerFragmentFactoryTest extends ActivityUnitTestCase<TestDefaultMainActivity> {


    public LigerFragmentFactoryTest(Class<TestDefaultMainActivity> activityClass) {
        super(activityClass);
    }

    public void testFragmentFactoryReturnOfNavigatorFragment() throws Exception {
        String pageName = "navigator";
        String title = "Page Title";
        JSONObject pageArgs = new JSONObject("{\"accessibilityLabel\":\"firstPage\",\"page\":\"firstPage\",\"args\":{\"hello\":\"world\"},\"title\":\"First Page\"}");
        JSONObject pageOptions = new JSONObject("{}");

        PageFragment navigatorFragment = LigerFragmentFactory.openPage(pageName,title,pageArgs,pageOptions);

        assertTrue(navigatorFragment instanceof NavigatorFragment);
    }

    public void testFragmentFactoryReturnOfDrawerFragment() throws Exception {
        String pageName = "drawer";
        String title = "Page Title";
        JSONObject pageArgs = new JSONObject("{\"accessibilityLabel\":\"firstPage\",\"page\":\"firstPage\",\"args\":{\"hello\":\"world\"},\"title\":\"First Page\"}");
        JSONObject pageOptions = new JSONObject("{}");

        PageFragment drawerFragment = LigerFragmentFactory.openPage(pageName,title,pageArgs,pageOptions);

        assertTrue(drawerFragment instanceof DrawerFragment);
    }

    public void testFragmentFactoryReturnOfAppMenuFragment() throws Exception {
        String pageName = "appMenu";
        String title = "Page Title";
        JSONObject pageArgs = new JSONObject("{\"accessibilityLabel\":\"firstPage\",\"page\":\"firstPage\",\"args\":{\"hello\":\"world\"},\"title\":\"First Page\"}");
        JSONObject pageOptions = new JSONObject("{}");

        PageFragment appMenuFragment = LigerFragmentFactory.openPage(pageName,title,pageArgs,pageOptions);

        assertTrue(appMenuFragment instanceof LigerAppMenuFragment);
    }

    public void testFragmentFactoryReturnOfCordovaPageFragment() throws Exception {
        String pageName = "webpage";
        String title = "Page Title";
        JSONObject pageArgs = new JSONObject("{\"accessibilityLabel\":\"firstPage\",\"page\":\"firstPage\",\"args\":{\"hello\":\"world\"},\"title\":\"First Page\"}");
        JSONObject pageOptions = new JSONObject("{}");

        PageFragment webPageFragment = LigerFragmentFactory.openPage(pageName,title,pageArgs,pageOptions);

        assertTrue(webPageFragment instanceof CordovaPageFragment);
    }

    public void testFragmentFactoryLaunchingIntents(){
        String[] SUPPORTED_INTENTS = {"email", "browser", "message", "image", "twitter", "facebook", "sinaweibo", "tencentweibo"};

        for (String intent : SUPPORTED_INTENTS) {
            PageFragment shouldBeNull = LigerFragmentFactory.openPage(intent,"Some Intent", null, null);
            assertNull(shouldBeNull);

            Intent launchIntent = getStartedActivityIntent();
            assertNotNull("Intent was null", launchIntent);
            assertTrue(isFinishCalled());

        }
    }


}