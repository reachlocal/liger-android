package com.reachlocal.mobile.liger.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.gcm.GcmRegistrationHelper;
import com.reachlocal.mobile.liger.listeners.RootPageListener;
import com.reachlocal.mobile.liger.model.AppConfig;
import com.reachlocal.mobile.liger.utils.LigerJSONObject;
import com.reachlocal.mobile.liger.widgets.MenuInterface;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultMainActivity extends ActionBarActivity implements CordovaInterface, GcmRegistrationHelper.OnGcmRegisteredListener, RootPageListener {

    public static final String SAVE_CHILD_ARGS = "SAVE_CHILD_ARGS";

    public ActionBarDrawerToggle menuToggle;

    private String lastChildArgs;
    protected CordovaPlugin activityResultCallback;

    protected PageFragment mRootPageFragment;


    public DrawerLayout menuDrawer;

    protected boolean activityResultKeepRunning = false;
    protected boolean keepRunning = false;
    protected final ExecutorService threadPool = Executors.newCachedThreadPool();

    private final BroadcastReceiver dynamicReceiver
            = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            // TODO Handle C2DM
            JSONObject payload = new JSONObject();
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                try {
                    // json.put(key, bundle.get(key)); see edit below
                    payload.put(key, LigerJSONObject.wrap(extras.get(key)));
                } catch(JSONException e) {
                    //Handle exception here
                }
            }
            // blocks passing broadcast to StaticReceiver instance
            mRootPageFragment.sendJavascript("PAGE.notificationArrived(" + payload.toString() + ",false);");
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dynamicReceiver);
    }
    

    @Override
    protected void onResume() {
        super.onResume();

        Intent callingintent = getIntent();
        Bundle extras = callingintent.getExtras();
        extras.getString("message_type_id");
        extras.getString("message");
        String myString = extras.getString("extrasString");
        Log.e("EXTRAS", extras.toString());

        final IntentFilter filter = new
                IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.addCategory("com.example.gcm");
        filter.setPriority(1);
        registerReceiver(dynamicReceiver, filter,
                "com.google.android.c2dm.permission.SEND", null);

    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConfig mAppConfig = AppConfig.getAppConfig(this);

        JSONObject rootPageArgs = mAppConfig.getRootPageArgs();
        JSONObject subPageArgs = null;
        try {
           subPageArgs = rootPageArgs.getJSONObject("args");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent callingintent = getIntent();
        Bundle extras = callingintent.getExtras();
        if(extras != null && subPageArgs != null) {
            Intent cloudintent = (Intent) extras.get("cloudIntent");
            if(cloudintent != null) {
                JSONObject payload = new JSONObject();
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    try {
                        // json.put(key, bundle.get(key)); see edit below
                        payload.put(key, LigerJSONObject.wrap(extras.get(key)));
                    } catch (JSONException e) {
                        //Handle exception here
                    }
                }

                try {
                    subPageArgs.put("notification", payload);
                    rootPageArgs.put("args", subPageArgs);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        LigerFragmentFactory.mContext = this;
        mRootPageFragment = LigerFragmentFactory.openPage(mAppConfig.getRootPageName(), mAppConfig.getRootPageTitle(), rootPageArgs, mAppConfig.getRootPageOptions());
        
        if( mRootPageFragment instanceof DrawerFragment){
            setContentView(R.layout.liger_main_drawer);
        } else {
            setContentView(R.layout.liger_main_frame);
        }       
        
        if (mRootPageFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            mRootPageFragment.addFragments(ft, R.id.content_frame);
            mRootPageFragment.setRootPageListener(this);
            ft.commit();
        }
        if (savedInstanceState != null) {
            lastChildArgs = savedInstanceState.getString(SAVE_CHILD_ARGS);
        }
        Config.init(this);

        if (mAppConfig.getNotificationsEnabled()) {
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
                if (menuDrawer != null && menuDrawer.isDrawerOpen(Gravity.START)) {
                    menuDrawer.closeDrawers();
                } else {
                    closePage(null, null);
                }
                return true;
            }
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

    @Override
    public Object onMessage(String id, Object data) {
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @Override
    public void onGcmRegistered(String registrationId, String errorMessage) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "Received GCM registration ID:" + registrationId);
        }
        //String args = JSUtils.stringListToArgString(registrationId, "AndroidPushToken", errorMessage);

        mRootPageFragment.pushNotificationTokenUpdated(registrationId, errorMessage);
    }

    @Override
    public void onFragmentFinished(PageFragment page) {
        if (page == mRootPageFragment) {
            finish();
        }
    }

    public PageFragment getRootPageFragment() {
        return mRootPageFragment;
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "DefaultMainActivity openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }
        if(menuDrawer != null){
            menuDrawer.closeDrawers();
        }
        mRootPageFragment.openPage(pageName, title, pageArgs, pageOptions);
    }

    public void openDialog(String pageName, String title, JSONObject args, JSONObject options) {
        if(menuDrawer != null){
            menuDrawer.closeDrawers();
        }
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG,
                    "DefaultMainActivity openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()) + ", options:"
                            + (options == null ? null : options.toString()));
        }
        mRootPageFragment.openDialog(pageName, title, args, options);
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


}
