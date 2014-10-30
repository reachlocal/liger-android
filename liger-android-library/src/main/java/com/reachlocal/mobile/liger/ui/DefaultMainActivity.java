package com.reachlocal.mobile.liger.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.gcm.GcmRegistrationHelper;
import com.reachlocal.mobile.liger.model.AppConfig;
import com.reachlocal.mobile.liger.utils.JSUtils;

import com.reachlocal.mobile.liger.widgets.MenuInterface;

import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultMainActivity extends ActionBarActivity implements CordovaInterface, GcmRegistrationHelper.OnGcmRegisteredListener {

    public static final String SAVE_CHILD_ARGS = "SAVE_CHILD_ARGS";

    public ActionBarDrawerToggle menuToggle;

    private String lastChildArgs;

    protected CordovaPlugin activityResultCallback;

    protected PageFragment mRootPageFragment;


    public DrawerLayout menuDrawer;

    protected boolean activityResultKeepRunning = false;
    protected boolean keepRunning = false;
    protected final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liger_main);
        AppConfig mAppConfig = AppConfig.getAppConfig(this);

        LigerFragmentFactory.mContext = this;
        mRootPageFragment = LigerFragmentFactory.openPage(mAppConfig.getRootPageName(), mAppConfig.getRootPageTitle(), mAppConfig.getRootPageArgs(), mAppConfig.getRootPageOptions());

        if(mRootPageFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mRootPageFragment.addFragments(ft, R.id.content_frame);
            ft.commit();
        }
        if (savedInstanceState != null) {
            lastChildArgs = savedInstanceState.getString(SAVE_CHILD_ARGS);
        }
        Config.init(this);

        if(mAppConfig.getNotificationsEnabled()) {
            GcmRegistrationHelper gcmHelper = new GcmRegistrationHelper(this, this);
            gcmHelper.registerGcm();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_CHILD_ARGS, lastChildArgs);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public Map<String, Object> getJavascriptInterfaces(PageFragment page) {
        return new HashMap<String, Object>();
    }

    public String getAssetBaseDirectory() {
        return "app";
    }

    public LigerWebClient createLigerWebClient(CordovaPageFragment fragment, CordovaWebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new LigerWebClient.LigerICSWebClient(fragment, this, webView);
        }
        return new LigerWebClient(fragment, this, webView);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (menuDrawer.isDrawerOpen(Gravity.START)) {
                    menuDrawer.closeDrawers();
                } else {
                    closePage(null, null);
                }

            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void closePage(PageFragment closePage, String closeTo) {
        mRootPageFragment.closeLastPage(closePage, closeTo);
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return
        // results
        if (command != null) {
            this.keepRunning = false;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //menuToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        menuToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "DefaultMainActivity.onCreateOptionsMenu()");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    public void sendJavascriptWithArgs(String object, String function, String args) {

        if (mRootPageFragment == null) {
            Log.w(LIGER.TAG, "Cannot send javascript, no current web fragment!");
        } else {
            mRootPageFragment.sendJavascriptWithArgs(object, function, args);
        }
    }

    @Override
    public Object onMessage(String id, Object data) {
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }




    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "openPage() pageName:" + pageName + ", args:" + pageArgs+ ", options:" + pageOptions);
        }
        menuDrawer.closeDrawers();
        mRootPageFragment.openPage(pageName,title,pageArgs,pageOptions);
    }

    public void openDialog(String pageName, String title, JSONObject args, JSONObject options) {
        menuDrawer.closeDrawers();
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG,
                    "DefaultMainActivity openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()) + ", options:"
                            + (options == null ? null : options.toString()));
        }
        mRootPageFragment.openDialog(pageName,title,args,options);
    }

    public void setMenuSelection(String pageName) {
        if (mRootPageFragment instanceof MenuInterface) {
            ((MenuInterface) mRootPageFragment).setSelectedItem(pageName);
        }
    }


    public void resetApp() {
        //TODO fragStack.resetToHome();
        //setMenuSelection(fragStack.getCurrentFragment().getPageName());
    }

    @Override
    public void onGcmRegistered(String registrationId, String errorMessage) {
        if(LIGER.LOGGING) {
            Log.d(LIGER.TAG, "Received GCM registration ID:" + registrationId);
        }
        String  args = JSUtils.stringListToArgString(registrationId, "AndroidPushToken", errorMessage);

        mRootPageFragment.sendJavascriptWithArgs("PAGE", "pushNotificationTokenUpdated", args);
    }

}
