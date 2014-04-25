package com.reachlocal.mobile.liger;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.*;

public class PageStackHelper {

    public static final String DIALOG_FRAGMENT = "ligerDialogFragment";

    private String mHomePageName;
    private String mHomePageTitle;

    private Set<String> mReusablePageNames = new HashSet<String>();
    private Map<String, PageFragment> mReusablePages = new HashMap<String, PageFragment>();

    private DefaultMainActivity mActivity;
    final private int mContentFrameId;

    private Deque<PageFragment> mFragDeck = new LinkedList<PageFragment>();

    public PageStackHelper(int contentFrameId) {
        this.mContentFrameId = contentFrameId;
    }

    public void onCreate(DefaultMainActivity activity, Bundle savedInstanceState, String homePageName, List<String> reusablePageNames) {
        this.mActivity = activity;
        this.mHomePageName = homePageName;
        if(reusablePageNames != null) {
            mReusablePageNames.addAll(reusablePageNames);
        }
        resetToHome();
    }

    public void openPage(String pageName, String title, JSONObject args) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "PageStackHelper.openPage() pageName:" + pageName + ", args:" + args);
        }
        //pageName = pageName.toLowerCase();

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();

        if (pageName.equals(mHomePageName)) {
            if (mFragDeck.size() == 0) {
                createAndAddPage(ft, pageName, title, args);
            } else {
                popTo(ft, mFragDeck.getFirst());
            }
        } else if (mReusablePageNames.contains(pageName.toLowerCase())) {
            PageFragment reuseFrag = findFragmentByPageName(pageName);
            if (reuseFrag != null && !reuseFrag.isVisible()) {
                mFragDeck.remove(reuseFrag);
                pushFragment(ft, reuseFrag);
            } else {
                createAndAddPage(ft, pageName, title, args);
            }
        } else {
            createAndAddPage(ft, pageName, title, args);
        }

        ft.commit();
        logStack("openPage " + pageName);
    }

    public void openDialog(String pageName, String title, JSONObject args) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG,
                    "PageStackHelper.openDialog() title:" + title + ", pageName:" + pageName + ", args:"
                            + (args == null ? null : args.toString()));
        }
        CordovaPageFragment dialog =  CordovaPageFragment.build(pageName, title, args);
        dialog.show(mActivity.getSupportFragmentManager(), DIALOG_FRAGMENT);
    }

    public PageFragment createPage(String pageName, String title, JSONObject args) {
        PageFragment pageFrag = null;
        boolean isReusable = false;
        if (mReusablePageNames.contains(pageName)) {
            isReusable = true;
            pageFrag = mReusablePages.get(pageName);
        }
        if(pageFrag == null) {
            pageFrag = CordovaPageFragment.build(pageName, title, args);
            if(isReusable) {
                mReusablePages.put(pageName, pageFrag);
            }
        }
        return pageFrag;
    }

    protected void createAndAddPage(FragmentTransaction ft, String pageName, String title, JSONObject args) {
        PageFragment newFragment = createPage(pageName, title, args);
        pushFragment(ft, newFragment);
    }

    public DefaultMainActivity getActivity() {
        return mActivity;
    }

    private void pushFragment(FragmentTransaction ft, PageFragment newCurrentFragment) {

        if (mFragDeck.size() > 0) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            PageFragment oldCurrentFrag = mFragDeck.getLast();
            ft.hide(oldCurrentFrag);
        }
        if (newCurrentFragment.isAdded()) {
            ft.show(newCurrentFragment);
        } else {
            ft.add(mContentFrameId, newCurrentFragment);
        }
        mFragDeck.addLast(newCurrentFragment);
    }

    private PageFragment findFragmentByPageName(String pageName) {
        for (PageFragment frag : mFragDeck) {
            if (frag.getPageName().equals(pageName)) {
                return frag;
            }
        }
        return null;
    }

    /**
     * @param ft
     * @param newTop The fragment that will be left at the top. Must be in the queue
     * @return true if any fragments were removed
     */
    private boolean popTo(FragmentTransaction ft, PageFragment newTop) {
        if (mFragDeck.size() > 0 && mFragDeck.getLast().equals(newTop)) {
            return false;
        }
        boolean hasRemoved = false;
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        while (mFragDeck.size() > 0) {
            PageFragment top = mFragDeck.getLast();
            if (top == newTop) {
                ft.show(newTop);
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

    public boolean hasPrevious() {
        return mFragDeck.size() > 1;
    }

    public String closeLastPage(PageFragment closePage, String closeTo) {
        PageFragment lastPage = mFragDeck.getLast();

        PageFragment parentPage = null;
        if (!StringUtils.isEmpty(closeTo)) {
            Iterator<PageFragment> it = mFragDeck.descendingIterator();
            while (it.hasNext()) {
                PageFragment candidate = it.next();
                if (StringUtils.equals(closeTo, candidate.getPageName())) {
                    parentPage = candidate;
                    break;
                }
            }
        }
        if(closePage == null || closePage == lastPage) {
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            mFragDeck.removeLast();
            lastPage.doPageClosed();
            ft.remove(lastPage);
            if (parentPage == null) {
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                parentPage = mFragDeck.getLast();
                ft.show(parentPage);
            } else {
                popTo(ft, parentPage);
            }
            String parentUpdateArgs = lastPage.getParentUpdateArgs();
            parentPage.setChildArgs(parentUpdateArgs);
            ft.commit();
            logStack("closeLastPage");
        }


        return parentPage == null ? null : parentPage.getPageName();
    }

    public PageFragment getCurrentFragment() {
        return mFragDeck.getLast();
    }

    private void logStack(String crumb) {
        if (LIGER.LOGGING) {
            StringBuffer sb = new StringBuffer("PageStackHelper/").append(crumb).append("\n");
            for (PageFragment frag : mFragDeck) {
                sb.append("\n-->").append(frag.getPageName()).append("/").append(frag.getPageArgs());
            }
            Log.d(LIGER.TAG, sb.toString());
        }
    }

    public void resetToHome() {
        mReusablePages.clear(); //If we are resetting, don't keep pages for reuse.
        mFragDeck.clear();

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        PageFragment homePage = createPage(mHomePageName, null, null);
        mFragDeck.addLast(homePage);
        ft.replace(mContentFrameId, homePage);
        ft.commit();
    }
}
