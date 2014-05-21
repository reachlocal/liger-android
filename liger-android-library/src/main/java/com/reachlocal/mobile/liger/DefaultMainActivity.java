package com.reachlocal.mobile.liger;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import com.reachlocal.mobile.liger.model.AppConfig;
import com.reachlocal.mobile.liger.widgets.DefaultMenuFragment;
import com.reachlocal.mobile.liger.widgets.MenuInterface;
import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultMainActivity extends ActionBarActivity implements CordovaInterface {

    public static final String SAVE_CHILD_ARGS = "SAVE_CHILD_ARGS";

    protected PageStackHelper fragStack;

    private String lastChildArgs;

    protected CordovaPlugin activityResultCallback;
    protected DrawerLayout menuDrawer;
    protected PageFragment mRootPageFragment;
    protected ActionBarDrawerToggle menuToggle;

    protected boolean activityResultKeepRunning = false;
    protected boolean keepRunning = false;
    protected final ExecutorService threadPool = Executors.newCachedThreadPool();

    private AppConfig mAppConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liger_main);
        mAppConfig = AppConfig.getAppConfig(this);
        List<String> pageNames = mAppConfig.getPageNames();

        fragStack = createPageStackHelper(R.id.content_frame);
        fragStack.onCreate(this, savedInstanceState, pageNames.get(0), pageNames);

        if (savedInstanceState != null) {
            lastChildArgs = savedInstanceState.getString(SAVE_CHILD_ARGS);
        }
        Config.init(this);
        setupMenu();
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

    public void closePage(PageFragment closedPage, String closeTo) {
        if (fragStack.hasPrevious()) {
            setMenuSelection(fragStack.closeLastPage(closedPage, closeTo));
        } else {
            finish();
        }
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
        menuToggle.syncState();
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
        if (menuToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    /**
     * Create the {@link PageStackHelper} which will be used to manage the
     * page stack and create and/or show pages. Subclasses can extend the functionality by returning
     * a custom subclass of {@link PageStackHelper}.
     *
     * @param contentFrameId
     * @return a new stack helper
     */
    public PageStackHelper createPageStackHelper(int contentFrameId) {
        return new PageStackHelper(contentFrameId);
    }

    public PageFragment getCurrentWebFrag() {
        return fragStack.getCurrentFragment();
    }

    public void sendJavascriptWithArgs(String object, String function, String args) {
        PageFragment currentWebFrag = fragStack.getCurrentFragment();
        if (currentWebFrag == null) {
            Log.w(LIGER.TAG, "Cannot send javascript, no current web fragment!");
        } else {
            currentWebFrag.sendJavascriptWithArgs(object, function, args);
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


    private void setupMenu() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        menuDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRootPageFragment = createRootPage();
        if (mRootPageFragment instanceof MenuInterface) {
            ((MenuInterface) mRootPageFragment).setMenuItems(mAppConfig.getMajorMenuItems(), mAppConfig.getMinorMenuItems());
        }
        getSupportFragmentManager().beginTransaction().add(R.id.drawer_menu, mRootPageFragment).commit();

        menuToggle = new ActionBarDrawerToggle(this, /* host Activity */
                menuDrawer, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.menu_open_desc, /* "open drawer" description for accessibility */
                R.string.menu_close_desc /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle(getTitle());
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getSupportActionBar().setTitle(getTitle());
                supportInvalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
                mRootPageFragment.doPageAppear();
            }
        };
        menuToggle.setDrawerIndicatorEnabled(true);

        menuDrawer.setDrawerListener(menuToggle);

    }

    public void openPage(String title, String screenName, JSONObject pageArgs) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "openPage() pageName:" + screenName + ", args:" + pageArgs);
        }
        menuDrawer.closeDrawers();
        fragStack.openPage(screenName, title, pageArgs);
        setMenuSelection(screenName);
    }

    public void openDialog(String pageName, String title, JSONObject args) {
        menuDrawer.closeDrawers();
        fragStack.openDialog(pageName, title, args);
    }

    private void setMenuSelection(String pageName) {
        if (mRootPageFragment instanceof MenuInterface) {
            ((MenuInterface) mRootPageFragment).setSelectedItem(pageName);
        }
    }

    /**
     * Creates a page fragment comprised of native components. Override to provide
     * a different page.
     *
     * @return
     */
    protected PageFragment createRootPage() {
        return new DefaultMenuFragment();
    }

    public void resetApp() {
        fragStack.resetToHome();
        setMenuSelection(fragStack.getCurrentFragment().getPageName());
    }

}
