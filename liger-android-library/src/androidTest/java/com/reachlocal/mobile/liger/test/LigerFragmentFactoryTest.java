package com.reachlocal.mobile.liger.test;

import android.test.InstrumentationTestCase;

import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.ui.CordovaPageFragment;
import com.reachlocal.mobile.liger.ui.DefaultMainActivity;
import com.reachlocal.mobile.liger.ui.LigerAppMenuFragment;
import com.reachlocal.mobile.liger.ui.LigerDrawerFragment;
import com.reachlocal.mobile.liger.ui.LigerNavigatorFragment;
import com.reachlocal.mobile.liger.ui.PageFragment;

import org.json.JSONObject;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class LigerFragmentFactoryTest extends InstrumentationTestCase {

    public void testFragmentFactoryReturnOfNavigatorFragment() throws Exception {
        String pageName = "navigator";
        String title = "Page Title";
        JSONObject pageArgs = new JSONObject("{\"accessibilityLabel\":\"firstPage\",\"page\":\"firstPage\",\"args\":{\"hello\":\"world\"},\"title\":\"First Page\"}");
        JSONObject pageOptions = new JSONObject("{}");

        PageFragment navigatorFragment = LigerFragmentFactory.openPage(pageName,title,pageArgs,pageOptions);

        assertTrue(navigatorFragment instanceof LigerNavigatorFragment);
    }

    public void testFragmentFactoryReturnOfDrawerFragment() throws Exception {
        String pageName = "drawer";
        String title = "Page Title";
        JSONObject pageArgs = new JSONObject("{\"accessibilityLabel\":\"firstPage\",\"page\":\"firstPage\",\"args\":{\"hello\":\"world\"},\"title\":\"First Page\"}");
        JSONObject pageOptions = new JSONObject("{}");

        PageFragment drawerFragment = LigerFragmentFactory.openPage(pageName,title,pageArgs,pageOptions);

        assertTrue(drawerFragment instanceof LigerDrawerFragment);
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
        LigerFragmentFactory.mContext = getInstrumentation().getContext();

        for (String intent : SUPPORTED_INTENTS) {
            PageFragment shouldBeNull = LigerFragmentFactory.openPage(intent,"Some Intent", null, null);
            assertNull(shouldBeNull);
        }
    }


}