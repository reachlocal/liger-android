package com.reachlocal.mobile.liger.testApp;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.reachlocal.mobile.liger.ApplicationState;
import com.reachlocal.mobile.liger.LIGER;
import com.reachlocal.mobile.liger.factories.LigerFragmentFactory;
import com.reachlocal.mobile.liger.ui.PageFragment;

import org.json.JSONObject;

/**
 * Created by Mark Wagner on 11/20/14.
 */
public class nativePages extends PageFragment {

    Button button1;
    Button button2;

    View.OnClickListener myhandler1 = new View.OnClickListener() {
        public void onClick(View v) {
            openPage("firstPage", "First Page", null, null);
        }
    };
    View.OnClickListener myhandler2 = new View.OnClickListener() {
        public void onClick(View v) {

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View page = inflater.inflate(R.layout.fragment_native_pages, container, false);
        button1 = (Button) page.findViewById(R.id.button1);
        button2 = (Button) page.findViewById(R.id.button2);
        button1.setOnClickListener(myhandler1);
        button2.setOnClickListener(myhandler2);
        return page;
    }

    @Override
    public void openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        if (LIGER.LOGGING) {
            Log.d(LIGER.TAG, "LigerNavigatorFragment openPage() pageName:" + pageName + ", args:" + pageArgs + ", options:" + pageOptions);
        }
        PageFragment page = null;


        page = LigerFragmentFactory.openPage(pageName, title, pageArgs, pageOptions);


        if (page != null) {
            page.doPageAppear();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            page.addFragments(ft, R.id.content_frame);
            ft.commit();
        }
    }

    @Override
    public String getPageName() {
        return null;
    }

    @Override
    public String getPageTitle() {
        return null;
    }

    @Override
    public void setToolbar(String toolbarSpec) {

    }

    @Override
    public void setChildArgs(String childUpdateArgs) {

    }

    @Override
    public void notificationArrived(JSONObject notificationPayload, ApplicationState applicationState) {

    }

    @Override
    public String getPageArgs() {
        return null;
    }

    @Override
    public String getParentUpdateArgs() {
        return null;
    }

    @Override
    public void setParentUpdateArgs(String parentUpdateArgs) {

    }

    @Override
    public void sendJavascript(String js) {

    }

    @Override
    public void addFragments(FragmentTransaction ft, int contentViewID) {
        ft.replace(contentViewID, this);
    }

    @Override
    public String closeLastPage(PageFragment closePage, String closeTo) {
        return null;
    }

    @Override
    protected PageFragment getChildPage() {
        return null;
    }
}
