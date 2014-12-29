package com.reachlocal.mobile.liger.ui;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.R;
import com.reachlocal.mobile.liger.listeners.PageLifecycleListener;
import com.reachlocal.mobile.liger.listeners.RootPageListener;
import com.reachlocal.mobile.liger.utils.CordovaUtils;

import org.apache.cordova.CallbackContext;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public abstract class PageFragment extends DialogFragment {

    String pageName;

    RootPageListener mActivity;

    protected FragmentActivity mContext;

    protected PageFragment mContainer = null;

    public static final String DIALOG_FRAGMENT = "ligerDialogFragment";

    protected Deque<PageFragment> mFragDeck = new LinkedList<PageFragment>();

    protected List<PageLifecycleListener> mLifecycleListeners = new ArrayList<PageLifecycleListener>();

    protected NotificationToken mTokenHolder;

    public static PageFragment fromCallbackContext(CallbackContext cc) {
        return CordovaUtils.fromCallbackContext(cc, R.id.web_view_parent_frag);
    }

    protected boolean popTo(FragmentTransaction ft, PageFragment newTop) {
        if (mFragDeck.size() > 0 && mFragDeck.getLast().equals(newTop)) {
            return false;
        }
        boolean hasRemoved = false;
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        while (mFragDeck.size() > 0) {
            PageFragment top = mFragDeck.getLast();
            if (top == newTop) {
                break;
            } else {
                mFragDeck.removeLast();
                top.doPageClosed();
                ft.remove(top);
                hasRemoved = true;
            }
        }
        logStack("popTo");
        return hasRemoved;
    }

    public void setRootPageListener(RootPageListener activity) {
        mActivity = activity;
    }

    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        throw new RuntimeException(this.getClass().getSimpleName() + " openPage Not Implemented");
    }

    public void openDialog(String pageName, String title, JSONObject args, JSONObject options) {
        throw new RuntimeException(this.getClass().getSimpleName() + " openDialog Not Implemented");
    }

    public PageFragment getContainer() {
        return mContainer;
    }

    public abstract String getPageName();

    public abstract String getPageTitle();

    public abstract void setToolbar(String toolbarSpec);

    public abstract void setChildArgs(String childUpdateArgs);

    public abstract String getPageArgs();

    public abstract String getParentUpdateArgs();

    public abstract void setParentUpdateArgs(String parentUpdateArgs);

    public abstract void sendJavascript(String js);

    public void doPageAppear() {
    }

    public abstract void addFragments(FragmentTransaction ft, int contentViewID);

    public void doPageClosed() {
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mContainer != null) {
            mContainer.fragmentDetached(this);
        }
    }

    @Override
    public void onDestroy() {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, this.getClass().getSimpleName() + ".onDestroy() " + pageName);
        }
        super.onDestroy();
    }


    protected void fragmentDetached(PageFragment detachedFrag) {
        boolean lastRemoved = false;
        if (mFragDeck.size() > 0) {
            PageFragment lastPage = mFragDeck.getLast();
            if (lastPage == detachedFrag) {
                //mFragDeck.removeLast();
                lastRemoved = true;
            }
        }

        if (lastRemoved || (mFragDeck.size() == 1 && mFragDeck.getLast().isDetached())) {
            if (!mContext.isFinishing()) {
//                FragmentTransaction ft = mContext.getSupportFragmentManager().beginTransaction();
//                ft.remove(this);
//                ft.commit();
            }
            if (mContainer != null) {
                mContainer.fragmentDetached(this);
            }
            if (mActivity != null) {
                mActivity.onFragmentFinished(this);
            }
        }
    }


    public abstract String closeLastPage(PageFragment closePage, String closeTo);

    protected void logStack(String crumb) {
        if (LIGER.LOGGING) {
            StringBuilder sb = new StringBuilder("PageFragment/").append(crumb).append("\n");
            for (PageFragment frag : mFragDeck) {
                sb.append("\n-->").append(frag.getPageName()).append("/").append(frag.getPageArgs());
            }
            Log.d(LIGER.TAG, sb.toString());
        }
    }

    // Functions that match javascript

    protected abstract PageFragment getChildPage();

    public void closeDialogArguments(String args) {
        if (getChildPage() != null)
            getChildPage().closeDialogArguments(args);
    }

    public void pushNotificationTokenUpdated(String registrationId, String errorMessage) {
        if (getChildPage() != null) {
            getChildPage().pushNotificationTokenUpdated(registrationId, errorMessage);
        } else {
            mTokenHolder = new NotificationToken(registrationId, errorMessage);
        }
    }

    protected class NotificationToken {
        public String registrationId;
        public String errorMessage;

        NotificationToken(String registyrationId, String errorMessage) {
            this.registrationId = registyrationId;
            this.errorMessage = errorMessage;
        }

    }
}
